package com.chac.feature.album.navigation

import androidx.navigation3.runtime.NavKey
import com.chac.feature.album.model.MediaUiModel
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
     * @param title 클러스터 제목
     * @param mediaList 갤러리에 표시할 미디어 목록
     */
    @Serializable
    data class Gallery(
        val title: String,
        val mediaList: List<MediaUiModel>,
    ) : AlbumNavKey
}
