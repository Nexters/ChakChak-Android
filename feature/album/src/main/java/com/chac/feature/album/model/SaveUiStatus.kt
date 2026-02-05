package com.chac.feature.album.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

/**
 * 클러스터 저장 상태.
 */
@Stable
@Serializable
sealed interface SaveUiStatus {
    @Immutable
    @Serializable
    data object Default : SaveUiStatus

    @Immutable
    @Serializable
    data object Saving : SaveUiStatus

    @Immutable
    @Serializable
    data object SaveCompleted : SaveUiStatus
}
