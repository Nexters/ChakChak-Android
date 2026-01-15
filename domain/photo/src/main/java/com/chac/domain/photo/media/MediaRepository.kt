package com.chac.domain.photo.media

interface MediaRepository {
    suspend fun getMedia(
        startTime: Long = 0,
        endTime: Long = System.currentTimeMillis(),
        mediaType: MediaType = MediaType.IMAGE,
        mediaSortOrder: MediaSortOrder = MediaSortOrder.NEWEST_FIRST,
    ): List<Media>

    suspend fun getMediaLocation(uri: String): MediaLocation?
}
