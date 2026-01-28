package com.chac.feature.album.model

import com.chac.domain.album.media.MediaCluster

/**
 * UI 클러스터를 도메인 모델로 변환한다.
 *
 * @receiver 변환 대상 UI 클러스터
 * @return 도메인 클러스터
 */
internal fun ClusterUiModel.toDomain(): MediaCluster = MediaCluster(
    id = id,
    title = title,
    mediaList = mediaList.map { it.toDomain() },
)
