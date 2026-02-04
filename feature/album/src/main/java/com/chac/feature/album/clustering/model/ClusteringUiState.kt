package com.chac.feature.album.clustering.model

import com.chac.feature.album.model.MediaClusterUiModel

sealed interface ClusteringUiState {
    /**
     * 클러스터 목록을 포함하는 상태.
     */
    sealed interface WithClusters : ClusteringUiState {
        /**
         * 화면에 표시할 클러스터 목록.
         */
        val clusters: List<MediaClusterUiModel>
    }

    /**
     * 로딩 중 상태.
     *
     * @property clusters 로딩 중에도 누적된 클러스터 목록
     */
    data class Loading(
        override val clusters: List<MediaClusterUiModel>,
    ) : WithClusters

    /**
     * 로딩 완료 상태.
     *
     * @property clusters 로딩이 끝난 클러스터 목록
     */
    data class Completed(
        override val clusters: List<MediaClusterUiModel>,
    ) : WithClusters

    /**
     * 권한 확인 중 상태.
     */
    data object PermissionChecking : ClusteringUiState

    /**
     * 권한 거부 상태.
     */
    data object PermissionDenied : ClusteringUiState
}
