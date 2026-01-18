package com.chac.domain.photo.media

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetClusteredMediaStreamUseCase
    @Inject
    constructor(
        private val mediaRepository: MediaRepository,
    ) {
        operator fun invoke(): Flow<MediaCluster> = mediaRepository.getClusteredMediaStream()
    }
