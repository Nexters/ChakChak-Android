package com.chac.domain.album.media.model

/**
 * 클러스터 저장 상태.
 */
sealed interface SaveStatus {
    data object Default : SaveStatus

    data object Saving : SaveStatus

    data object SaveCompleted : SaveStatus
}
