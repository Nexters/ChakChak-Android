package com.chac.feature.album.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chac.feature.album.gallery.model.GalleryUiState
import com.chac.feature.album.model.MediaUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** 갤러리 화면 상태를 제공하는 ViewModel */
class GalleryViewModel(
    /** 갤러리 화면에 표시할 앨범 제목 */
    private val title: String,
    /** 갤러리 화면에 표시할 미디어 목록 */
    private val mediaList: List<MediaUiModel>,
) : ViewModel() {
    /** 갤러리 화면 UI 상태 */
    private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.NoneSelected(title, mediaList))
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    /**
     * 미디어 선택 상태를 토글한다
     *
     * @param media 토글할 미디어
     */
    fun toggleSelection(media: MediaUiModel) {
        val selected = when (val state = _uiState.value) {
            is GalleryUiState.SomeSelected -> state.selectedIds
            is GalleryUiState.NoneSelected -> emptySet()
        }
        val updatedSelected = if (selected.contains(media.id)) {
            selected - media.id
        } else {
            selected + media.id
        }
        _uiState.value = if (updatedSelected.isEmpty()) {
            GalleryUiState.NoneSelected(title, mediaList)
        } else {
            GalleryUiState.SomeSelected(title, mediaList, updatedSelected)
        }
    }

    /**
     * 모든 미디어를 선택 상태로 만든다
     */
    fun selectAll() {
        val updatedSelected = mediaList.map { it.id }.toSet()
        _uiState.value = if (updatedSelected.isEmpty()) {
            GalleryUiState.NoneSelected(title, mediaList)
        } else {
            GalleryUiState.SomeSelected(title, mediaList, updatedSelected)
        }
    }

    /** 선택된 미디어를 모두 해제한다 */
    fun clearSelection() {
        _uiState.value = GalleryUiState.NoneSelected(title, mediaList)
    }

    companion object {
        /**
         * 화면 인자를 전달하기 위한 ViewModel Factory
         *
         * @param title 갤러리 화면에 표시할 앨범 제목
         * @param mediaList 갤러리 화면에 표시할 미디어 목록
         */
        fun provideFactory(
            title: String,
            mediaList: List<MediaUiModel>,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = GalleryViewModel(title, mediaList) as T
        }
    }
}
