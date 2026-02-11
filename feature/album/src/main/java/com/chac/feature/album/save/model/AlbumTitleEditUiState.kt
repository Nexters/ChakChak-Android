package com.chac.feature.album.save.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.TextFieldValue

/**
 * 앨범명 수정 화면의 UI 상태.
 *
 * @param isInitialized [com.chac.feature.album.save.AlbumTitleEditViewModel.initialize]가 완료되면 true(선택이 비어있어도 완료로 간주).
 * @param title 현재 제목 입력값(Compose [androidx.compose.ui.text.input.TextFieldValue]는 selection/composition 상태를 포함).
 * @param placeholder 앨범명의 placeholder
 * @param selectedCount 선택된 미디어 개수.
 * @param selectedUriStrings 선택된 전체 항목의 URI 문자열 목록(UI 레이어에서 사용).
 * @param isSaving 저장 요청이 진행 중이면 true(버튼/액션 비활성화 용도).
 */
@Immutable
data class AlbumTitleEditUiState(
    val isInitialized: Boolean = false,
    val title: TextFieldValue = TextFieldValue(""),
    val placeholder: String = "",
    val selectedCount: Int = 0,
    val selectedUriStrings: List<String> = emptyList(),
    val isSaving: Boolean = false,
)
