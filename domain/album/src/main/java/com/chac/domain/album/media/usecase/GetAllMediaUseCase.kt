package com.chac.domain.album.media.usecase

import com.chac.domain.album.media.model.Media
import com.chac.domain.album.media.repository.MediaRepository
import javax.inject.Inject

/**
 * 전체 미디어 목록을 반환한다.
 *
 * 현재 구현에서는 클러스터링에서 사용하는 MediaRepository.getMedia() 기본 조건과 동일하게 조회한다.
 */
class GetAllMediaUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
) {
    suspend operator fun invoke(): List<Media> = mediaRepository.getMedia()
}
