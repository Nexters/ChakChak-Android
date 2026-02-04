package com.chac.data.album.media

import com.chac.data.album.media.clustering.ClusteringStrategy
import com.chac.data.album.media.clustering.di.LocationBasedClustering
import com.chac.data.album.media.clustering.di.TimeBasedClustering
import com.chac.data.album.media.reversGeocoder.ReverseGeocoder
import com.chac.data.album.media.timeformat.TimeFormatProvider
import com.chac.domain.album.media.model.Media
import com.chac.domain.album.media.model.MediaCluster
import com.chac.domain.album.media.model.MediaLocation
import com.chac.domain.album.media.repository.MediaRepository
import com.chac.domain.album.media.model.MediaSortOrder
import com.chac.domain.album.media.model.MediaType
import com.chac.domain.album.media.model.SaveStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject

/**
 * 시간 기준으로 먼저 클러스터링한 뒤 위치 기준으로 다시 클러스터링하는 Media repository.
 *
 * 계산된 클러스터에는 중심 좌표를 리버스 지오코딩한 제목이 포함된다.
 * [getClusteredMediaStream]은 계산 결과를 캐시에 저장해 이후 수집자에게 재사용한다.
 */
internal class MediaRepositoryImpl @Inject constructor(
    @TimeBasedClustering
    private val timeBasedClusteringStrategy: ClusteringStrategy,
    @LocationBasedClustering
    private val locationBasedClusteringStrategy: ClusteringStrategy,
    private val reverseGeocoder: ReverseGeocoder,
    private val timeFormatProvider: TimeFormatProvider,
    private val dataSource: MediaDataSource,
) : MediaRepository {
    /** 캐시된 클러스터 스냅샷을 외부에 전달하는 상태 Flow (계산 전에는 null) */
    private val _clusteredMediaState = MutableStateFlow<List<MediaCluster>?>(null)
    override val clusteredMediaState: StateFlow<List<MediaCluster>?> = _clusteredMediaState

    override fun getClusteredMediaStream(): Flow<MediaCluster> = flow {
        // 현재 스냅샷이 있으면 재계산 없이 그대로 방출한다.
        val cached = _clusteredMediaState.value
        if (cached != null) {
            cached.forEach { cluster ->
                emit(cluster)
            }
            return@flow
        }

        val result = createClusteredMedia { cluster ->
            emit(cluster)
        }

        _clusteredMediaState.value = result
    }

    private suspend fun createClusteredMedia(
        onCluster: suspend (MediaCluster) -> Unit = {},
    ): List<MediaCluster> {
        val starTime = System.currentTimeMillis()
        Timber.d("MediaRepositoryImpl, getClusteredMedia call")

        val timeBasedClusters = timeBasedClusteringStrategy.cluster(getMedia())

        val step1Time = System.currentTimeMillis()
        Timber.d(
            "MediaRepositoryImpl, time base clusters-${timeBasedClusters.size}," +
                "mediaCounts-${timeBasedClusters.values.flatten().size}, time = ${step1Time - starTime}",
        )

        // TODO 이렇게 캐싱 없이 한번에 exif에서 LatLng를 가져오는 방식은 오래걸려서 개선해야함.
        val mediaClusters = mutableListOf<MediaCluster>()
        timeBasedClusters.values.forEach { mediaInTimeCluster ->
            val locationBasedClusters = locationBasedClusteringStrategy.cluster(mediaInTimeCluster)
            locationBasedClusters.forEach { (keyTime, mediaList) ->
                val title = getClusterTitle(mediaList)
                val cluster = MediaCluster(
                    id = keyTime,
                    mediaList = mediaList,
                    title = title,
                )
                mediaClusters.add(cluster)
                onCluster(cluster)
            }
        }

        val step2Time = System.currentTimeMillis()
        Timber.d(
            "MediaRepositoryImpl, locationBasedClusters-${mediaClusters.size}," +
                "mediaCounts-${mediaClusters.flatMap { it.mediaList }.size}, time = ${step2Time - step1Time}",
        )

        return mediaClusters
    }

    override suspend fun getMedia(
        startTime: Long,
        endTime: Long,
        mediaType: MediaType,
        mediaSortOrder: MediaSortOrder,
    ): List<Media> = dataSource.getMedia(
        startTime = startTime,
        endTime = endTime,
        mediaType = mediaType,
        mediaSortOrder = mediaSortOrder,
    )

    override suspend fun getMediaLocation(uri: String): MediaLocation? = dataSource.getMediaLocation(uri)

    override suspend fun saveAlbum(
        cluster: MediaCluster,
    ): List<Media> {
        val mediaList = cluster.mediaList
        if (mediaList.isEmpty()) return emptyList()

        // 같은 미디어는 하나의 클러스터에만 속한다는 전제 하에 대상 클러스터만 갱신한다.
        val targetClusterId = cluster.id
        val previousStatus = _clusteredMediaState.value
            ?.firstOrNull { it.id == targetClusterId }
            ?.saveStatus
        if (previousStatus != null) {
            // 대상 클러스터를 Saving 상태로 전환한다.
            _clusteredMediaState.update { clusters ->
                clusters?.map { cached ->
                    if (cached.id == targetClusterId) {
                        cached.copy(saveStatus = SaveStatus.Saving)
                    } else {
                        cached
                    }
                }
            }
        }

        // 앨범을 저장하고 저장된 미디어 리스트를 반환받는다.
        val savedMedia = dataSource.saveAlbum(cluster.title, mediaList)

        if (savedMedia.isEmpty()) {
            if (previousStatus != null) {
                // 저장 결과가 없으면 이전 상태로 되돌린다.
                _clusteredMediaState.update { clusters ->
                    clusters?.map { cached ->
                        if (cached.id != targetClusterId) return@map cached
                        if (cached.saveStatus == previousStatus) {
                            cached
                        } else {
                            cached.copy(saveStatus = previousStatus)
                        }
                    }
                }
            }

            return emptyList()
        }

        val savedIds = savedMedia.map { it.id }.toHashSet()
        // 대상 클러스터만 저장된 항목을 제거하고 완료 상태로 전환한다.
        _clusteredMediaState.update { clusters ->
            clusters?.map { cached ->
                if (cached.id != targetClusterId) return@map cached
                val filtered = cached.mediaList.filterNot { it.id in savedIds }
                cached.copy(
                    mediaList = filtered,
                    saveStatus = SaveStatus.SaveCompleted,
                )
            }
        }

        return savedMedia
    }

    private suspend fun getClusterTitle(mediaList: List<Media>): String {
        if (mediaList.isEmpty()) return ""
        val address = getClusterCentroid(mediaList)
            ?.let { centroid -> reverseGeocoder.reverseGeocode(centroid).orEmpty() }
            .orEmpty()

        val formattedTime = timeFormatProvider.formatClusterTime(mediaList.first().dateTaken)

        return if (address.isBlank()) {
            formattedTime
        } else {
            "$formattedTime $address"
        }
    }

    private suspend fun getClusterCentroid(mediaList: List<Media>): MediaLocation? {
        var sumLatitude = 0.0
        var sumLongitude = 0.0
        var count = 0
        mediaList.forEach { media ->
            val location = dataSource.getMediaLocation(media.uriString)
            val latitude = location?.latitude
            val longitude = location?.longitude
            if (latitude != null && longitude != null) {
                sumLatitude += latitude
                sumLongitude += longitude
                count += 1
            }
        }

        if (count == 0) return null
        return MediaLocation(
            latitude = sumLatitude / count,
            longitude = sumLongitude / count,
        )
    }
}
