package com.chac.feature.album.navigation

import androidx.navigation3.runtime.NavKey
import com.chac.feature.album.model.MediaClusterUiModel
import kotlinx.serialization.Serializable

/** 앨범 기능에서 사용하는 NavKey 정의 */
@Serializable
sealed interface AlbumNavKey : NavKey {
    /** 클러스터링 목록 화면 */
    @Serializable
    data object Clustering : AlbumNavKey

    /**
     * 갤러리 화면
     *
     * @param cluster 갤러리에 표시할 클러스터
     */
    @Serializable
    data class Gallery(
        val cluster: MediaClusterUiModel,
    ) : AlbumNavKey

    // TODO: Gallery와 함께 cluster 대신 clusterId로 조회하는 방식으로 전환

    /**
     * 미디어 미리보기 화면
     *
     * @param cluster 클러스터
     * @param mediaId 최초 표시할 미디어 식별자
     */
    @Serializable
    data class MediaPreview(
        val cluster: MediaClusterUiModel,
        val mediaId: Long,
    ) : AlbumNavKey

    /**
     * 앨범 저장 완료 화면
     *
     * @param title 저장된 앨범 제목
     * @param savedCount 저장된 사진 개수
     */
    @Serializable
    data class SaveCompleted(
        val title: String,
        val savedCount: Int,
    ) : AlbumNavKey

    /** 설정 화면 */
    @Serializable
    data object Settings : AlbumNavKey

    /** 온보딩 화면 */
    @Serializable
    data object Onboarding : AlbumNavKey
}
