package com.chac.domain.album.media.usecase

import javax.inject.Inject

/**
 * 클러스터링 대상이 되는 미디어 총 개수를 반환한다.
 */
class GetAllMediaCountUseCase @Inject constructor(
    private val getAllMediaUseCase: GetAllMediaUseCase,
) {
    suspend operator fun invoke(): Int = getAllMediaUseCase().size
}
