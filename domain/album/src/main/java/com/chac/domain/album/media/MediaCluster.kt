package com.chac.domain.album.media

/**
 * 클러스터링 결과를 표현하는 모델.
 *
 * @param id 클러스터 대표 시간(예: 첫 미디어의 dateTaken)
 * @param mediaList 클러스터에 포함된 미디어 목록
 */
data class MediaCluster(
    val id: Long,
    val mediaList: List<Media>,
)
