package com.chac.data.album.media

import com.chac.data.album.media.clustering.ClusteringStrategy
import com.chac.data.album.media.clustering.di.LocationBasedClustering
import com.chac.data.album.media.clustering.di.TimeBasedClustering
import com.chac.data.album.media.reversGeocoder.ReverseGeocoder
import com.chac.domain.album.media.Media
import com.chac.domain.album.media.MediaCluster
import com.chac.domain.album.media.MediaLocation
import com.chac.domain.album.media.MediaRepository
import com.chac.domain.album.media.MediaSortOrder
import com.chac.domain.album.media.MediaType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
    private val dataSource: MediaDataSource,
) : MediaRepository {
    /** [cachedClusteredMedia]에 접근할 때 동시성 문제를 방지하기 위한 Lock 객체 */
    private val cacheLock = Any()

    /** 클러스터의 캐시 데이터 */
    private var cachedClusteredMedia: List<MediaCluster>? = null

    override fun getClusteredMediaStream(): Flow<MediaCluster> = flow {
        val cached = synchronized(cacheLock) { cachedClusteredMedia }
        if (cached != null) {
            cached.forEach { cluster ->
                emit(cluster)
            }
            return@flow
        }

        val result = createClusteredMedia { cluster ->
            emit(cluster)
        }
        synchronized(cacheLock) {
            cachedClusteredMedia = result
        }
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

    private suspend fun getClusterTitle(mediaList: List<Media>): String {
        val centroid = getClusterCentroid(mediaList) ?: return ""
        return reverseGeocoder.reverseGeocode(centroid).orEmpty()
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
