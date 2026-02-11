package com.chac.domain.album.media.usecase

import com.chac.domain.album.media.ClusteringWorkScheduler
import javax.inject.Inject

class CancelClusteringUseCase @Inject constructor(
    private val clusteringWorkScheduler: ClusteringWorkScheduler,
) {
    operator fun invoke() {
        clusteringWorkScheduler.cancelClustering()
    }
}
