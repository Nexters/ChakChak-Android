package com.chac.feature.album.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * 클러스터 UI 표현 모델.
 *
 * @property id 클러스터 식별자
 * @property address 클러스터 주소
 * @property formattedDate 클러스터 날짜
 * @property mediaList 클러스터에 포함된 미디어 UI 모델 목록
 * @property thumbnailUriStrings 클러스터 썸네일 URI 문자열 목록
 */
@Immutable
@Serializable
data class MediaClusterUiModel(
    val id: Long,
    val address: String,
    val formattedDate: String,
    val mediaList: List<MediaUiModel>,
    val thumbnailUriStrings: List<String>,
)
