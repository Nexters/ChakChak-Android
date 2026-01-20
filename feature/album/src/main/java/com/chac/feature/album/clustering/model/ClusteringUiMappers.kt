package com.chac.feature.album.clustering.model

import com.chac.domain.album.media.MediaCluster
import com.chac.feature.album.model.ClusterUiModel
import com.chac.feature.album.model.toUiModel

/**
 * 도메인 클러스터를 UI 모델로 변환한다.
 *
 * @receiver 변환 대상 도메인 클러스터
 * @return 클러스터 UI 모델
 */
internal fun MediaCluster.toUiModel(): ClusterUiModel = ClusterUiModel(
    id = id,
    title = title,
    mediaList = mediaList.map { it.toUiModel() },
)
