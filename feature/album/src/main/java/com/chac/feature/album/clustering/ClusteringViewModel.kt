package com.chac.feature.album.clustering

import androidx.lifecycle.ViewModel

/** 클러스터링 화면 상태를 제공하는 ViewModel */
class ClusteringViewModel : ViewModel() {
    /** 클러스터링 목록에 표시할 임시 데이터 */
    val clusters = listOf(
        ClusterItem(
            title = "Cluster 1",
            photos = listOf("Photo 1-1", "Photo 1-2", "Photo 1-3"),
        ),
        ClusterItem(
            title = "Cluster 2",
            photos = listOf("Photo 2-1", "Photo 2-2"),
        ),
        ClusterItem(
            title = "Cluster 3",
            photos = listOf("Photo 3-1"),
        ),
    )
}

/**
 * (임시) 클러스터링 화면에서 사용하는 간단한 클러스터 모델
 *
 * @param title 클러스터 제목
 * @param photos 클러스터에 포함된 사진 목록
 */
data class ClusterItem(
    val title: String,
    val photos: List<String>,
)
