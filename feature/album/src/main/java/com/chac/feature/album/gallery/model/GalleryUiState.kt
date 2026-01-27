package com.chac.feature.album.gallery.model

import com.chac.feature.album.model.MediaUiModel

/** 갤러리 화면 선택 상태를 표현하는 UI 상태 */
sealed interface GalleryUiState {
    /**
     * 아무 것도 선택되지 않은 상태
     *
     * @property title 화면에 표시할 앨범 제목
     * @property mediaList 화면에 표시할 미디어 목록
     */
    data class NoneSelected(
        val title: String,
        val mediaList: List<MediaUiModel>,
    ) : GalleryUiState

    /**
     * 하나 이상 선택된 상태
     *
     * @property title 화면에 표시할 앨범 제목
     * @property mediaList 화면에 표시할 미디어 목록
     * @property selectedIds 선택된 미디어 ID 목록
     */
    data class SomeSelected(
        val title: String,
        val mediaList: List<MediaUiModel>,
        val selectedIds: Set<Long>,
    ) : GalleryUiState
}
