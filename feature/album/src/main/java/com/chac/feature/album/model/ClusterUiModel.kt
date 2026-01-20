package com.chac.feature.album.model

import androidx.compose.runtime.Immutable

/**
 * 클러스터 UI 표현 모델.
 *
 * @property id 클러스터 식별자
 * @property title 클러스터 제목
 * @property mediaList 클러스터에 포함된 미디어 UI 모델 목록
 */
@Immutable
data class ClusterUiModel(
    val id: Long,
    val title: String,
    val mediaList: List<MediaUiModel>,
)
