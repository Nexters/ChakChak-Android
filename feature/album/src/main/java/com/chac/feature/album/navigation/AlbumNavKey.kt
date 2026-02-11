package com.chac.feature.album.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** 앨범 기능에서 사용하는 NavKey 정의 */
@Serializable
sealed interface AlbumNavKey : NavKey {
    /** 클러스터링 목록 화면 */
    @Serializable
    data object Clustering : AlbumNavKey

    /** 전체 사진 갤러리 화면 */
    @Serializable
    data object AllPhotosGallery : AlbumNavKey

    /**
     * 갤러리 화면
     *
     * @param clusterId 갤러리에 표시할 클러스터 ID
     */
    @Serializable
    data class Gallery(
        val clusterId: Long,
    ) : AlbumNavKey

    /**
     * 미디어 미리보기 화면
     *
     * @param clusterId 클러스터 ID
     * @param mediaId 최초 표시할 미디어 식별자
     */
    @Serializable
    data class MediaPreview(
        val clusterId: Long,
        val mediaId: Long,
    ) : AlbumNavKey

    /**
     * 전체 사진 미디어 미리보기 화면
     *
     * @param mediaId 최초 표시할 미디어 식별자
     */
    @Serializable
    data class AllPhotosMediaPreview(
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

    /**
     * 앨범명 수정 화면
     *
     * @param clusterId 저장 대상 클러스터 ID (전체 사진 저장의 경우 null)
     * @param selectedMediaIds 저장 대상 미디어 ID 목록
     */
    @Serializable
    data class AlbumTitleEdit(
        val clusterId: Long?,
        val selectedMediaIds: List<Long>,
    ) : AlbumNavKey

    /** 설정 화면 */
    @Serializable
    data object Settings : AlbumNavKey
}
