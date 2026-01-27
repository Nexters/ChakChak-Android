package com.chac.domain.album.media

import javax.inject.Inject

class StartClusteringUseCase @Inject constructor(
    private val clusteringWorkScheduler: ClusteringWorkScheduler
) {
    operator fun invoke() {
        clusteringWorkScheduler.scheduleClustering()
    }
}
