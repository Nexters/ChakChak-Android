package com.chac.domain.album.media

import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    suspend fun getClusteredMedia(): Map<Long, List<Media>>

    /** 클러스터 단위로 계산 결과를 emit하는 Flow */
    fun getClusteredMediaStream(): Flow<MediaCluster> = TODO("Implement paging in data layer")

    suspend fun getMedia(
        startTime: Long = 0,
        endTime: Long = System.currentTimeMillis(),
        mediaType: MediaType = MediaType.IMAGE,
        mediaSortOrder: MediaSortOrder = MediaSortOrder.NEWEST_FIRST,
    ): List<Media>

    suspend fun getMediaLocation(uri: String): MediaLocation?
}
