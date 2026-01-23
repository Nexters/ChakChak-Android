package com.chac.data.album.media.clustering

import android.content.Context
import com.chac.data.album.media.getMediaLocation
import com.chac.domain.album.media.Media
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.apache.commons.math3.ml.clustering.DBSCANClusterer
import org.apache.commons.math3.ml.clustering.DoublePoint
import org.apache.commons.math3.ml.distance.EuclideanDistance
import javax.inject.Inject

class LocationBasedClusteringStrategy @Inject constructor(
    @ApplicationContext private val context: Context,
) : ClusteringStrategy() {
    private val epsilon = DEFAULT_EPSILON
    private val minPoints = DEFAULT_MIN_POINTS
    private val batchSize = LOCATION_FETCH_BATCH_SIZE

    override suspend fun performClustering(mediaList: List<Media>): Map<Long, List<Media>> {
        if (mediaList.isEmpty()) return emptyMap()

        // 위치 정보 병렬 처리 및 위치 정보가 있는 미디어만 필터링
        val mediaWithLocations = coroutineScope {
            mediaList.chunked(batchSize).flatMap { batch ->
                batch
                    .map { media ->
                        async {
                            val location = getMediaLocation(context, media.uriString)
                            if (location?.latitude != null && location.longitude != null) {
                                Pair(media, location)
                            } else {
                                null
                            }
                        }
                    }.awaitAll()
                    .filterNotNull()
            }
        }

        if (mediaWithLocations.isEmpty()) return emptyMap()

        // DBSCAN 클러스터링을 위한 LocationPoint 생성
        val locationPoints = mediaWithLocations.map { (media, location) ->
            LocationPoint(
                doubleArrayOf(
                    location.latitude ?: 0.0,
                    location.longitude ?: 0.0,
                ),
                media,
            )
        }

        // Apache Commons Math3의 DBSCANClusterer를 사용하여 클러스터링 수행
        val clusterer = DBSCANClusterer<LocationPoint>(
            epsilon,
            minPoints,
            EuclideanDistance(),
        )

        val clusters = clusterer.cluster(locationPoints)

        // 결과 맵 구성 (키: 클러스터 내 첫 번째 미디어의 dateTaken, 값: 미디어 리스트)
        val resultClusters = mutableMapOf<Long, List<Media>>()
        clusters.forEachIndexed { _, cluster ->
            if (cluster.points.isNotEmpty()) {
                val clusterMedia = cluster.points.map { it.media }
                // 클러스터의 첫 번째 미디어의 dateTaken을 키로 사용
                val clusterKey = clusterMedia.first().dateTaken
                resultClusters[clusterKey] = clusterMedia
            }
        }

        return resultClusters
    }

    // 위치 정보와 미디어를 함께 가지는 DoublePoint 확장 클래스
    private class LocationPoint(
        points: DoubleArray,
        val media: Media,
    ) : DoublePoint(points)

    companion object {
        const val DEFAULT_EPSILON: Double = 0.03
        const val DEFAULT_MIN_POINTS: Int = 1
        const val LOCATION_FETCH_BATCH_SIZE: Int = 20
    }
}
