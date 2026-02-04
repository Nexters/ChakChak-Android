package com.chac.domain.album.media.usecase

import com.chac.domain.album.media.model.MediaCluster
import com.chac.domain.album.media.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 클러스터링 결과를 스트림으로 제공한다.
 *
 * @return 클러스터 UI 표시를 위한 스트림
 */
class GetClusteredMediaStreamUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
) {
    operator fun invoke(): Flow<MediaCluster> = mediaRepository.getClusteredMediaStream()
}
