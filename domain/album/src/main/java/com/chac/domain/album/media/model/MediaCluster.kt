package com.chac.domain.album.media.model

/**
 * 클러스터링 결과를 표현하는 모델.
 *
 * @param id 클러스터 대표 시간(예: 첫 미디어의 dateTaken)
 * @param mediaList 클러스터에 포함된 미디어 목록
 * @param address 클러스터의 중심 좌표를 리버스 지오코딩한 주소
 * @param formattedDate 클러스터의 날짜를 포맷팅한 문자열
 */
data class MediaCluster(
    val id: Long,
    val mediaList: List<Media>,
    val address: String,
    val formattedDate: String,
)
