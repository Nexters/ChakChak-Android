package com.chac.feature.album.gallery.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chac.domain.album.media.usecase.GetClusteredMediaStateUseCase
import com.chac.feature.album.mapper.toUiModel
import com.chac.feature.album.model.MediaUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 미디어 미리보기 화면 상태를 제공하는 ViewModel */
@HiltViewModel
class MediaPreviewViewModel @Inject constructor(
    private val getClusteredMediaStateUseCase: GetClusteredMediaStateUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MediaPreviewUiState>(MediaPreviewUiState.Loading)
    val uiState: StateFlow<MediaPreviewUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    /**
     * 클러스터 ID와 미디어 ID로 미리보기 상태를 초기화한다.
     *
     * @param clusterId 클러스터 ID
     * @param mediaId 최초 표시할 미디어 식별자
     */
    fun initialize(clusterId: Long, mediaId: Long) {
        if (_uiState.value is MediaPreviewUiState.Ready) return
        if (loadJob != null) return

        loadJob = viewModelScope.launch {
            try {
                getClusteredMediaStateUseCase().collect { clusters ->
                    val cluster = clusters.firstOrNull { it.id == clusterId }?.toUiModel() ?: return@collect

                    val initialIndex = cluster.mediaList
                        .indexOfFirst { it.id == mediaId }
                        .coerceAtLeast(0)

                    _uiState.value = MediaPreviewUiState.Ready(
                        mediaList = cluster.mediaList,
                        initialIndex = initialIndex,
                        address = cluster.address,
                    )
                    // 최초 로드 후엔 추가 업데이트를 받지 않아도 된다.
                    loadJob?.cancel()
                }
            } finally {
                loadJob = null
            }
        }
    }
}

/** 미디어 미리보기 화면 UI 상태 */
sealed interface MediaPreviewUiState {
    data object Loading : MediaPreviewUiState

    data class Ready(
        val mediaList: List<MediaUiModel>,
        val initialIndex: Int,
        val address: String,
    ) : MediaPreviewUiState
}
