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

        /**
         * 클러스터링 대상이 되는 전체 사진 개수.
         *
         * 클러스터 스트림 수집 여부와 무관하게 고정된 값이다.
         */
        val allPhotosCount: Int
    }

    /**
     * 로딩 중 상태.
     *
     * @property clusters 로딩 중에도 누적된 클러스터 목록
     */
    data class Loading(
        override val clusters: List<MediaClusterUiModel>,
        override val allPhotosCount: Int,
    ) : WithClusters

    /**
     * 로딩 완료 상태.
     *
     * @property clusters 로딩이 끝난 클러스터 목록
     */
    data class Completed(
        override val clusters: List<MediaClusterUiModel>,
        override val allPhotosCount: Int,
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
