package com.chac.domain.album.media.usecase

import com.chac.domain.album.media.model.Media
import com.chac.domain.album.media.repository.MediaRepository
import javax.inject.Inject

/**
 * 클러스터링 대상이 되는 전체 미디어 목록을 조회한다.
 *
 * Repository의 기본 파라미터(기간/정렬/타입)를 사용한다.
 */
class GetAllMediaUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
) {
    suspend operator fun invoke(): List<Media> = mediaRepository.getMedia()
}

