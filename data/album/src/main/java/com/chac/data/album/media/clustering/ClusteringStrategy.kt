package com.chac.data.album.media.clustering

import com.chac.domain.album.media.model.Media
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class ClusteringStrategy {
    protected val minClusterSize: Int = DEFAULT_MIN_CLUSTER_SIZE

    suspend fun cluster(mediaList: List<Media>): Map<Long, List<Media>> {
        if (mediaList.isEmpty()) {
            return emptyMap()
        }

        return withContext(Dispatchers.IO) {
            val rawClusters = performClustering(mediaList)
            filterByMinSize(rawClusters)
        }
    }

    protected abstract suspend fun performClustering(mediaList: List<Media>): Map<Long, List<Media>>

    protected fun filterByMinSize(clusters: Map<Long, List<Media>>): Map<Long, List<Media>> = clusters.filter { (_, cluster) ->
        cluster.size >= minClusterSize
    }

    companion object {
        const val DEFAULT_MIN_CLUSTER_SIZE: Int = 20
    }
}
