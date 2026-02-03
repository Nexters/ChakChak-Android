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
}
