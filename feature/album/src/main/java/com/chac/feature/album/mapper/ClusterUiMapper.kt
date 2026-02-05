package com.chac.feature.album.mapper

import com.chac.domain.album.media.model.SaveStatus
import com.chac.domain.album.media.model.MediaCluster
import com.chac.feature.album.model.SaveUiStatus
import com.chac.feature.album.model.MediaClusterUiModel

/**
 * 도메인 클러스터를 UI 모델로 변환한다.
 *
 * @receiver 변환 대상 도메인 클러스터
 * @return 클러스터 UI 모델
 */
internal fun MediaCluster.toUiModel(): MediaClusterUiModel = MediaClusterUiModel(
    id = id,
    title = title,
    mediaList = mediaList.map { it.toUiModel() },
    thumbnailUriStrings = listOfNotNull(
        mediaList.getOrNull(0)?.uriString,
        mediaList.getOrNull(1)?.uriString,
    ),
    saveStatus = when (saveStatus) {
        SaveStatus.Default -> SaveUiStatus.Default
        SaveStatus.Saving -> SaveUiStatus.Saving
        SaveStatus.SaveCompleted -> SaveUiStatus.SaveCompleted
    },
)

/**
 * UI 클러스터를 도메인 모델로 변환한다.
 *
 * @receiver 변환 대상 UI 클러스터
 * @return 도메인 클러스터
 */
internal fun MediaClusterUiModel.toDomain(): MediaCluster = MediaCluster(
    id = id,
    title = title,
    mediaList = mediaList.map { it.toDomain() },
)
