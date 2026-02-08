package com.chac.domain.album.media.usecase

import com.chac.domain.album.media.model.Media
import com.chac.domain.album.media.repository.MediaRepository
import javax.inject.Inject

/**
 * 지정한 앨범명으로 선택한 미디어를 저장한다.
 */
class SaveAlbumWithTitleUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
) {
    suspend operator fun invoke(
        title: String,
        mediaList: List<Media>,
    ): List<Media> = mediaRepository.saveAlbum(title, mediaList)
}

