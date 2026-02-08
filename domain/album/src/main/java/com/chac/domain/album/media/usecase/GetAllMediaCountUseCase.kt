package com.chac.domain.album.media.usecase

import javax.inject.Inject

/**
 * 전체 미디어 개수를 조회한다.
 *
 * 실제 데이터 조회는 [GetAllMediaUseCase]에 위임한다.
 */
class GetAllMediaCountUseCase @Inject constructor(
    private val getAllMediaUseCase: GetAllMediaUseCase,
) {
    suspend operator fun invoke(): Int = getAllMediaUseCase().size
}

