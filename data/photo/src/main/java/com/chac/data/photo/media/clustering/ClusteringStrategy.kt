package com.chac.data.photo.media.clustering

import com.chac.domain.photo.media.Media

abstract class ClusteringStrategy {
    protected val minClusterSize: Int = DEFAULT_MIN_CLUSTER_SIZE

    fun cluster(mediaList: List<Media>): Map<Long, List<Media>> {
        if (mediaList.isEmpty()) {
            return emptyMap()
        }

        val rawClusters = performClustering(mediaList)

        return filterByMinSize(rawClusters)
    }

    protected abstract fun performClustering(mediaList: List<Media>): Map<Long, List<Media>>

    protected fun filterByMinSize(clusters: Map<Long, List<Media>>): Map<Long, List<Media>> =
        clusters.filter { (_, cluster) ->
            cluster.size >= minClusterSize
        }

    companion object {
        const val DEFAULT_MIN_CLUSTER_SIZE: Int = 20
    }
}
