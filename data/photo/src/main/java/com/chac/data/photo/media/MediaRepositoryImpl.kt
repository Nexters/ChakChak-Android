package com.chac.data.photo.media

import com.chac.domain.photo.media.Media
import com.chac.domain.photo.media.MediaLocation
import com.chac.domain.photo.media.MediaRepository
import com.chac.domain.photo.media.MediaSortOrder
import com.chac.domain.photo.media.MediaType
import javax.inject.Inject

internal class MediaRepositoryImpl
    @Inject
    constructor(
        private val dataSource: MediaDataSource,
    ) : MediaRepository {
        override suspend fun getMedia(
            startTime: Long,
            endTime: Long,
            mediaType: MediaType,
            mediaSortOrder: MediaSortOrder,
        ): List<Media> =
            dataSource.getMedia(
                startTime = startTime,
                endTime = endTime,
                mediaType = mediaType,
                mediaSortOrder = mediaSortOrder,
            )

        override suspend fun getMediaLocation(uri: String): MediaLocation? = dataSource.getMediaLocation(uri)
    }
