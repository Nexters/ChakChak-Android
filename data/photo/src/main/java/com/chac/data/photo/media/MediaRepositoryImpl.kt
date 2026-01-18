package com.chac.data.photo.media

import com.chac.data.photo.media.clustering.ClusteringStrategy
import com.chac.data.photo.media.clustering.di.LocationBasedClustering
import com.chac.data.photo.media.clustering.di.TimeBasedClustering
import com.chac.domain.photo.media.Media
import com.chac.domain.photo.media.MediaCluster
import com.chac.domain.photo.media.MediaLocation
import com.chac.domain.photo.media.MediaRepository
import com.chac.domain.photo.media.MediaSortOrder
import com.chac.domain.photo.media.MediaType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

internal class MediaRepositoryImpl
    @Inject
    constructor(
        @TimeBasedClustering
        private val timeBasedClusteringStrategy: ClusteringStrategy,
        @LocationBasedClustering
        private val locationBasedClusteringStrategy: ClusteringStrategy,
        private val dataSource: MediaDataSource,
    ) : MediaRepository {
        private val cacheLock = Any()
        private var cachedClusteredMedia: Map<Long, List<Media>>? = null

        override fun getClusteredMediaStream(): Flow<MediaCluster> =
            flow {
                val cached = synchronized(cacheLock) { cachedClusteredMedia }
                if (cached != null) {
                    cached.forEach { (keyTime, mediaList) ->
                        emit(
                            MediaCluster(
                                id = keyTime,
                                mediaList = mediaList,
                            ),
                        )
                    }
                    return@flow
                }

                val timeBasedClusters = timeBasedClusteringStrategy.cluster(getMedia())
                val locationBasedClusters = mutableMapOf<Long, List<Media>>()

                timeBasedClusters.values.forEach { mediaInTimeCluster ->
                    // TODO 이렇게 캐싱 없이 한번에 exif에서 LatLng를 가져오는 방식은 오래걸려서 개선해야함.
                    val locationClusters = locationBasedClusteringStrategy.cluster(mediaInTimeCluster)
                    locationClusters.forEach { (keyTime, mediaList) ->
                        locationBasedClusters[keyTime] = mediaList
                        emit(
                            MediaCluster(
                                id = keyTime,
                                mediaList = mediaList,
                            ),
                        )
                    }
                }

                synchronized(cacheLock) {
                    cachedClusteredMedia = locationBasedClusters.toMap()
                }
            }

        override suspend fun getClusteredMedia(): Map<Long, List<Media>> = getClusteredMediaInternal()

        private suspend fun getClusteredMediaInternal(): Map<Long, List<Media>> {
            val cached = synchronized(cacheLock) { cachedClusteredMedia }
            if (cached != null) return cached

            val starTime = System.currentTimeMillis()
            Timber.d("MediaRepositoryImpl, getClusteredMedia call")

            val timeBasedClusters = timeBasedClusteringStrategy.cluster(getMedia())

            val step1Time = System.currentTimeMillis()
            Timber.d(
                "MediaRepositoryImpl, time base clusters-${timeBasedClusters.size}," +
                    "mediaCounts-${timeBasedClusters.values.flatten().size}, time = ${step1Time - starTime}",
            )

            // TODO 이렇게 캐싱 없이 한번에 exif에서 LatLng를 가져오는 방식은 오래걸려서 개선해야함.
            val locationBasedClusters = mutableMapOf<Long, List<Media>>()
            timeBasedClusters.values.forEach { mediaInTimeCluster ->
                val locationClusters = locationBasedClusteringStrategy.cluster(mediaInTimeCluster)
                locationBasedClusters.putAll(locationClusters)
            }

            val step2Time = System.currentTimeMillis()
            Timber.d(
                "MediaRepositoryImpl, locationBasedClusters-${locationBasedClusters.size}," +
                    "mediaCounts-${locationBasedClusters.values.flatten().size}, time = ${step2Time - step1Time}",
            )

            val computed = locationBasedClusters.toMap()
            synchronized(cacheLock) {
                cachedClusteredMedia = computed
            }
            return computed
        }

        override suspend fun getMedia(
            startTime: Long,
            endTime: Long,
            mediaType: MediaType,
            mediaSortOrder: MediaSortOrder,
        ): List<Media> =
            dataSource.getMedia(
                startTime = startTime,
                endTime = endTime,
                mediaType = mediaType,
                mediaSortOrder = mediaSortOrder,
            )

        override suspend fun getMediaLocation(uri: String): MediaLocation? = dataSource.getMediaLocation(uri)
    }
