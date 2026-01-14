package com.chac.feature.album.navigation

import androidx.navigation3.runtime.NavKey
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
     * @param photos 갤러리에 표시할 사진 목록
     */
    @Serializable
    data class Gallery(
        val photos: List<String>,
    ) : AlbumNavKey
}
