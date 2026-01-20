package com.chac.feature.album.clustering.model

import com.chac.feature.album.model.ClusterUiModel

sealed interface ClusteringUiState {
    /**
     * 화면에 표시할 클러스터 목록.
     */
    val clusters: List<ClusterUiModel>

    /**
     * 로딩 중 상태.
     *
     * @property clusters 로딩 중에도 누적된 클러스터 목록
     */
    data class Loading(
        override val clusters: List<ClusterUiModel>,
    ) : ClusteringUiState

    /**
     * 로딩 완료 상태.
     *
     * @property clusters 로딩이 끝난 클러스터 목록
     */
    data class Completed(
        override val clusters: List<ClusterUiModel>,
    ) : ClusteringUiState
}
