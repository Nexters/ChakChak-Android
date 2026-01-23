package com.chac.feature.album.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** 갤러리 화면 상태를 제공하는 ViewModel */
class GalleryViewModel(
    /** 갤러리 화면에 표시할 사진 목록 */
    val photos: List<String>,
) : ViewModel() {
    /** 갤러리 화면 타이틀 */
    val title: String = "Gallery"

    companion object {
        /**
         * 화면 인자를 전달하기 위한 ViewModel Factory
         *
         * @param photos 갤러리 화면에 표시할 사진 목록
         */
        fun provideFactory(photos: List<String>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = GalleryViewModel(photos) as T
        }
    }
}
