package com.chac.domain.album.media

import com.chac.domain.album.media.model.ClusteringWorkState
import kotlinx.coroutines.flow.Flow

interface ClusteringWorkScheduler {
    fun scheduleClustering(promptText: String? = null)

    fun observeClusteringWorkState(): Flow<ClusteringWorkState>

    fun cancelClustering()
}
