package com.chac.domain.album.media.usecase

import com.chac.domain.album.media.ClusteringWorkScheduler
import com.chac.domain.album.media.model.ClusteringWorkState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveClusteringWorkStateUseCase @Inject constructor(
    private val clusteringWorkScheduler: ClusteringWorkScheduler,
) {
    operator fun invoke(): Flow<ClusteringWorkState> = clusteringWorkScheduler.observeClusteringWorkState()
}
