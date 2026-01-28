package com.chac.domain.album.media

import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    /** 클러스터 단위로 계산 결과를 emit하는 Flow */
    fun getClusteredMediaStream(): Flow<MediaCluster>

    suspend fun getMedia(
        startTime: Long = 0,
        endTime: Long = System.currentTimeMillis(),
        mediaType: MediaType = MediaType.IMAGE,
        mediaSortOrder: MediaSortOrder = MediaSortOrder.NEWEST_FIRST,
    ): List<Media>

    suspend fun getMediaLocation(uri: String): MediaLocation?

    /**
     * 앨범으로 미디어를 저장한다
     *
     * @param cluster 저장할 클러스터
     * @return 저장된 미디어 리스트
     */
    suspend fun saveAlbum(cluster: MediaCluster): List<Media>
}
