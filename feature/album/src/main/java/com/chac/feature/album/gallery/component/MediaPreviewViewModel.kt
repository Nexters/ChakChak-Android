package com.chac.feature.album.gallery.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chac.domain.album.media.usecase.GetAllMediaStateUseCase
import com.chac.domain.album.media.usecase.GetClusteredMediaStateUseCase
import com.chac.feature.album.mapper.toUiModel
import com.chac.feature.album.model.MediaUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 미디어 미리보기 화면 상태를 제공하는 ViewModel */
@HiltViewModel
class MediaPreviewViewModel @Inject constructor(
    private val getClusteredMediaStateUseCase: GetClusteredMediaStateUseCase,
    private val getAllMediaStateUseCase: GetAllMediaStateUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MediaPreviewUiState>(MediaPreviewUiState.Loading)
    val uiState: StateFlow<MediaPreviewUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

    private inline fun launchOnce(
        crossinline block: suspend () -> Unit,
    ) {
        if (_uiState.value is MediaPreviewUiState.Ready) return
        if (loadJob != null) return

        loadJob = viewModelScope.launch {
            try {
                block()
            } finally {
                loadJob = null
            }
        }
    }

    /**
     * 클러스터 ID와 미디어 ID로 미리보기 상태를 초기화한다.
     *
     * @param clusterId 클러스터 ID
     * @param mediaId 최초 표시할 미디어 식별자
     */
    fun initialize(clusterId: Long, mediaId: Long) {
        launchOnce {
            val cluster = getClusteredMediaStateUseCase()
                .map { clusters -> clusters.firstOrNull { it.id == clusterId }?.toUiModel() }
                .filterNotNull()
                .first()

            if (cluster.mediaList.isEmpty()) return@launchOnce

            val initialIndex = cluster.mediaList
                .indexOfFirst { it.id == mediaId }
                .coerceAtLeast(0)

            _uiState.value = MediaPreviewUiState.Ready(
                mediaList = cluster.mediaList,
                initialIndex = initialIndex,
                address = cluster.address,
            )
        }
    }

    /**
     * 전체 사진 모드로 미리보기 상태를 초기화한다.
     *
     * @param mediaId 최초 표시할 미디어 식별자
     */
    fun initializeAllPhotos(mediaId: Long) {
        launchOnce {
            val uiMediaList = getAllMediaStateUseCase().first()
                .map { it.toUiModel() }

            if (uiMediaList.isEmpty()) return@launchOnce

            val initialIndex = uiMediaList
                .indexOfFirst { it.id == mediaId }
                .coerceAtLeast(0)

            _uiState.value = MediaPreviewUiState.Ready(
                mediaList = uiMediaList,
                initialIndex = initialIndex,
                address = "",
            )
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
