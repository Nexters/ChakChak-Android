package com.chac.domain.album.media.repository

import com.chac.domain.album.media.model.MediaSortOrder
import com.chac.domain.album.media.model.Media
import com.chac.domain.album.media.model.MediaCluster
import com.chac.domain.album.media.model.MediaLocation
import com.chac.domain.album.media.model.MediaType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MediaRepository {
    /** 클러스터 단위로 계산 결과를 emit하는 Flow */
    fun getClusteredMediaStream(): Flow<MediaCluster>

    /** 캐시된 전체 클러스터 스냅샷을 제공하는 상태 Flow (계산 전에는 null) */
    val clusteredMediaState: StateFlow<List<MediaCluster>?>

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
