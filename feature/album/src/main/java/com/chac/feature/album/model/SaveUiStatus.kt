package com.chac.feature.album.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * 클러스터 저장 상태.
 */
@Stable
sealed interface SaveUiStatus {
    @Immutable
    data object Default : SaveUiStatus

    @Immutable
    data object Saving : SaveUiStatus

    @Immutable
    data object SaveCompleted : SaveUiStatus
}
