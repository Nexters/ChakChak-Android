package com.chac.domain.photo.media

import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    suspend fun getClusteredMedia(): Map<Long, List<Media>>

    fun getClusteredMediaStream(): Flow<MediaCluster>

    suspend fun getMedia(
        startTime: Long = 0,
        endTime: Long = System.currentTimeMillis(),
        mediaType: MediaType = MediaType.IMAGE,
        mediaSortOrder: MediaSortOrder = MediaSortOrder.NEWEST_FIRST,
    ): List<Media>

    suspend fun getMediaLocation(uri: String): MediaLocation?
}
