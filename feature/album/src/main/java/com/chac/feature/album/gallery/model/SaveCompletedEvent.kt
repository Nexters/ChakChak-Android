package com.chac.feature.album.gallery.model

/**
 * 저장 완료 이벤트를 전달한다.
 *
 * @param title 저장된 앨범 제목
 * @param savedCount 저장된 미디어 개수
 */
data class SaveCompletedEvent(
    val title: String,
    val savedCount: Int,
)
