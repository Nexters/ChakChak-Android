package com.chac.domain.album.media.usecase

import com.chac.domain.album.media.model.Media
import com.chac.domain.album.media.model.MediaCluster
import com.chac.domain.album.media.repository.MediaRepository
import javax.inject.Inject

/**
 * 선택한 미디어를 앨범으로 저장한다
 *
 * @param mediaRepository 미디어 저장을 담당하는 레포지토리
 */
class SaveAlbumUseCase @Inject constructor(
    private val mediaRepository: MediaRepository,
) {
    suspend operator fun invoke(
        cluster: MediaCluster,
        albumTitle: String,
    ): List<Media> = mediaRepository.saveAlbum(cluster, albumTitle)
}
