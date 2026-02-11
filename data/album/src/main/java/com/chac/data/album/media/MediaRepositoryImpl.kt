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

    /** 캐시된 전체 미디어 스냅샷을 외부에 전달하는 상태 Flow (계산 전에는 null) */
    private val _allMediaState = MutableStateFlow<List<Media>?>(null)
    override val allMediaState: StateFlow<List<Media>?> = _allMediaState

    /** 저장 처리로 인해 제거된 미디어 ID를 누적한다. (클러스터 생성 중 재노출 방지) */
    private val removedMediaIdsState = MutableStateFlow<Set<Long>>(emptySet())

    override fun getClusteredMediaStream(): Flow<MediaCluster> = flow {
        // 현재 스냅샷이 있으면 재계산 없이 그대로 방출한다.
        val cached = _clusteredMediaState.value
        if (cached != null) {
            cached.forEach { cluster ->
                emit(cluster)
            }
            return@flow
        }

        createClusteredMedia { cluster ->
            val removedIds = removedMediaIdsState.value
            val filteredMedia = cluster.mediaList.filterNot { it.id in removedIds }
            if (filteredMedia.isEmpty()) return@createClusteredMedia

            val filteredCluster = cluster.copy(mediaList = filteredMedia)
            emit(filteredCluster)
            // 클러스터링 중에도 saveAlbum()이 상태를 수정할 수 있도록 점진적으로 반영한다.
            _clusteredMediaState.update { (it ?: emptyList()) + filteredCluster }
        }
    }

    private suspend fun createClusteredMedia(
        onCluster: suspend (MediaCluster) -> Unit = {},
    ): List<MediaCluster> {
        val starTime = System.currentTimeMillis()
        Timber.d("MediaRepositoryImpl, getClusteredMedia call")

        val allMedia = getMedia()
        _allMediaState.value = allMedia
        val timeBasedClusters = timeBasedClusteringStrategy.cluster(allMedia)

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
                val address = getClusterAddress(mediaList)
                val formattedDate = if (mediaList.isNotEmpty()) {
                    timeFormatProvider.formatClusterTime(mediaList.first().dateTaken)
                } else {
                    ""
                }
                val cluster = MediaCluster(
                    id = keyTime,
                    mediaList = mediaList,
                    address = address,
                    formattedDate = formattedDate,
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
        albumTitle: String,
    ): List<Media> {
        val mediaList = cluster.mediaList
        if (mediaList.isEmpty()) return emptyList()

        val savedMedia = dataSource.saveAlbum(albumTitle, mediaList)
        if (savedMedia.isEmpty()) return emptyList()

        val savedIds = savedMedia.map { it.id }.toHashSet()
        removedMediaIdsState.update { it + savedIds }
        // 대상 클러스터에서 저장된 항목을 제거하고, 비어 있으면 클러스터 자체를 삭제한다.
        _clusteredMediaState.update { clusters ->
            clusters?.mapNotNull { cached ->
                // 선택된 미디어가 여러 클러스터에 걸쳐 있을 수 있으므로, 모든 클러스터에서 제거한다.
                val remaining = cached.mediaList.filterNot { it.id in savedIds }
                if (remaining.isEmpty()) null else cached.copy(mediaList = remaining)
            }
        }
        _allMediaState.update { cached ->
            cached?.filterNot { it.id in savedIds }
        }

        return savedMedia
    }

    private suspend fun getClusterAddress(mediaList: List<Media>): String {
        if (mediaList.isEmpty()) return ""
        return getClusterCentroid(mediaList)
            ?.let { centroid -> reverseGeocoder.reverseGeocode(centroid).orEmpty() }
            .orEmpty()
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
