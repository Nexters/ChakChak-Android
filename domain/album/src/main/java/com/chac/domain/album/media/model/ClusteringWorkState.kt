package com.chac.domain.album.media.model

enum class ClusteringWorkState {
    Idle,
    Enqueued,
    Running,
    Succeeded,
    Failed,
    Cancelled,
}
