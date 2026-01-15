package com.chac.feature.album.clustering

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chac.domain.photo.media.Media
import com.chac.domain.photo.media.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 클러스터링 화면 상태를 제공하는 ViewModel */
@HiltViewModel
class ClusteringViewModel
    @Inject
    constructor(
        private val mediaRepository: MediaRepository,
    ) : ViewModel() {
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

        val mediaState = mutableStateOf(emptyList<MediaUiModel>())

        init {
            viewModelScope.launch {
                val media = mediaRepository.getMedia()
                mediaState.value = media.map(Media::toUiModel)
            }
        }
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

@Immutable
data class MediaUiModel(
    val id: Long,
    val uriString: String,
)

private fun Media.toUiModel(): MediaUiModel = MediaUiModel(id = id, uriString = uriString)
