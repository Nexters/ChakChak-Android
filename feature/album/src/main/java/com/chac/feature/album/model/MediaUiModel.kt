package com.chac.feature.album.model

import androidx.compose.runtime.Immutable
import com.chac.domain.album.media.model.MediaType
import kotlinx.serialization.Serializable

/**
 * 미디어 UI 표현 모델.
 *
 * @property id 미디어 식별자
 * @property uriString 미디어 URI 문자열
 * @property dateTaken 촬영 시각(에포크 밀리초)
 * @property mediaType 미디어 타입
 */
@Immutable
@Serializable
data class MediaUiModel(
    val id: Long,
    val uriString: String,
    val dateTaken: Long,
    val mediaType: MediaType,
)
