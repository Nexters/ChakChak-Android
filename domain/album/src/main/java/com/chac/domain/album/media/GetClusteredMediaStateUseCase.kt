package com.chac.domain.album.media

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 클러스터링 결과 스냅샷을 상태 Flow로 제공한다.
 *
 * Repository의 null 상태는 빈 리스트로 변환한다.
 */
class GetClusteredMediaStateUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
) {
    operator fun invoke(): Flow<List<MediaCluster>> = mediaRepository.clusteredMediaState
        .map { it.orEmpty() }
}
