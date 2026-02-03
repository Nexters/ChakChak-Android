package com.chac.data.album.media.clustering

import com.chac.domain.album.media.model.Media
import javax.inject.Inject

class TimeBasedClusteringStrategy @Inject constructor() : ClusteringStrategy() {
    private val maxTimeDifference = DEFAULT_MAX_TIME_DIFFERENCE

    override suspend fun performClustering(mediaList: List<Media>): Map<Long, List<Media>> {
        if (mediaList.isEmpty()) return emptyMap()

        // 최신순으로 정렬
        val sortedMedia = mediaList.sortedByDescending { it.dateTaken }

        // 결과 맵 (키: 클러스터 시작 시간, 값: 미디어 리스트)
        val resultClusters = mutableMapOf<Long, MutableList<Media>>()

        var currentClusterStartTime: Long? = null
        var currentCluster = mutableListOf<Media>()
        var remainingMedia = sortedMedia.size

        // 시간 차이에 따라 미디어 아이템 그룹화
        for (i in sortedMedia.indices) {
            val currentMedia = sortedMedia[i]

            // 새 클러스터 시작 여부 결정
            if (currentCluster.isEmpty()) {
                currentCluster.add(currentMedia)
                currentClusterStartTime = currentMedia.dateTaken
                remainingMedia--
                continue
            }

            val previousMedia = sortedMedia[i - 1]
            val timeDifference = previousMedia.dateTaken - currentMedia.dateTaken

            // 시간 차이가 임계값을 초과하면 현재 클러스터 처리
            if (timeDifference > maxTimeDifference) {
                // 현재 클러스터가 최소 크기를 충족하는 경우에만 결과에 추가
                if (currentCluster.size >= minClusterSize) {
                    resultClusters[currentClusterStartTime!!] = currentCluster
                }

                // 새 클러스터 시작 - 남은 미디어 항목이 최소 크기보다 적으면 더 이상 유효한 클러스터가 불가능
                if (remainingMedia < minClusterSize) {
                    // 더 이상 처리할 필요가 없으므로 반복 종료
                    break
                }

                // 새 클러스터 초기화
                currentCluster = mutableListOf(currentMedia)
                currentClusterStartTime = currentMedia.dateTaken
            } else {
                // 현재 클러스터에 추가
                currentCluster.add(currentMedia)
            }

            remainingMedia--
        }

        // 마지막 클러스터가 최소 크기를 충족하는 경우에만 결과에 추가
        if (currentCluster.size >= minClusterSize && currentClusterStartTime != null) {
            resultClusters[currentClusterStartTime] = currentCluster
        }

        return resultClusters
    }

    companion object {
        const val DEFAULT_MAX_TIME_DIFFERENCE: Long = 3 * 60 * 60 * 1000 // 3 hours in milliseconds
    }
}
