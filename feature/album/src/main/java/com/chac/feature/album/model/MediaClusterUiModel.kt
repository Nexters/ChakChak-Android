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
) {
    companion object {
        /**
         * "모든 사진" 화면에서 사용하는 가상 클러스터 ID.
         *
         * 실제 클러스터 ID는 양수 타임스탬프 기반이므로 음수 값을 사용한다.
         */
        const val ALL_PHOTOS_ID: Long = -1L

        /**
         * "모든 사진"로 화면을 이동할 때 사용할 MediaClusterUiModel
         *
         * TODO: 추 후 화면 이동시 ID만 전달하는 방식으로 변경 후 제거해야함
         */
        val allPhotos = MediaClusterUiModel(
            id = ALL_PHOTOS_ID,
            address = "",
            formattedDate = "",
            mediaList = emptyList(),
            thumbnailUriStrings = emptyList(),
        )
    }
}
