package com.chac.domain.album.media.usecase

import com.chac.domain.album.media.ClusteringWorkScheduler
import javax.inject.Inject

class StartClusteringUseCase @Inject constructor(
    private val clusteringWorkScheduler: ClusteringWorkScheduler,
) {
    operator fun invoke(promptText: String? = null) {
        clusteringWorkScheduler.scheduleClustering(promptText)
    }
}
