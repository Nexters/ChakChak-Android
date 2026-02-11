package com.chac.domain.album.media.usecase

import com.chac.domain.album.media.model.Media
import com.chac.domain.album.media.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 전체 미디어 스냅샷을 상태 Flow로 제공한다.
 *
 * Repository의 null 상태는 빈 리스트로 변환한다.
 */
class GetAllMediaStateUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
) {
    operator fun invoke(): Flow<List<Media>> = mediaRepository.allMediaState
        .map { it.orEmpty() }
}
