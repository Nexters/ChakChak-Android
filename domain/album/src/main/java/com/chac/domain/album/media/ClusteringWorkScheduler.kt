package com.chac.domain.album.media

import com.chac.domain.album.media.model.ClusteringWorkState
import kotlinx.coroutines.flow.Flow

interface ClusteringWorkScheduler {
    fun scheduleClustering()

    fun observeClusteringWorkState(): Flow<ClusteringWorkState>

    fun cancelClustering()
}
