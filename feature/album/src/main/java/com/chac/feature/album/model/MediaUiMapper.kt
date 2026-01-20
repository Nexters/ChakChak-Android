package com.chac.feature.album.model

import com.chac.domain.album.media.Media

/**
 * 도메인 미디어를 UI 모델로 변환한다.
 *
 * @receiver 변환 대상 도메인 미디어
 * @return 미디어 UI 모델
 */
internal fun Media.toUiModel(): MediaUiModel =
    MediaUiModel(
        id = id,
        uriString = uriString,
        dateTaken = dateTaken,
        mediaType = mediaType,
    )
