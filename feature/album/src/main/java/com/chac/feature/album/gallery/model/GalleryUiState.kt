package com.chac.feature.album.gallery.model

import com.chac.feature.album.model.ClusterUiModel

/** 갤러리 화면 선택 상태를 표현하는 UI 상태 */
sealed interface GalleryUiState {
    /** 화면에 표시할 클러스터 */
    val cluster: ClusterUiModel

    /**
     * 아무 것도 선택되지 않은 상태
     *
     * @property cluster 화면에 표시할 클러스터
     */
    data class NoneSelected(
        override val cluster: ClusterUiModel,
    ) : GalleryUiState

    /**
     * 하나 이상 선택된 상태
     *
     * @property cluster 화면에 표시할 클러스터
     * @property selectedIds 선택된 미디어 ID 목록
     */
    data class SomeSelected(
        override val cluster: ClusterUiModel,
        val selectedIds: Set<Long>,
    ) : GalleryUiState

    /**
     * 저장 중 상태
     *
     * @property cluster 화면에 표시할 클러스터
     * @property selectedIds 저장 중인 미디어 ID 목록
     */
    data class Saving(
        override val cluster: ClusterUiModel,
        val selectedIds: Set<Long>,
    ) : GalleryUiState
}
