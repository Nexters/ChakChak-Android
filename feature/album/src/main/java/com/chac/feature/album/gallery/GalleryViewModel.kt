package com.chac.feature.album.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chac.domain.album.media.GetClusteredMediaStateUseCase
import com.chac.domain.album.media.SaveAlbumUseCase
import com.chac.feature.album.clustering.model.toUiModel
import com.chac.feature.album.gallery.model.GalleryUiState
import com.chac.feature.album.gallery.model.SaveCompletedEvent
import com.chac.feature.album.model.ClusterUiModel
import com.chac.feature.album.model.MediaUiModel
import com.chac.feature.album.model.toDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 갤러리 화면 상태를 제공하는 ViewModel */
@HiltViewModel
class GalleryViewModel @Inject constructor(
    /** 앨범 저장 유즈케이스 */
    private val saveAlbumUseCase: SaveAlbumUseCase,
    private val getClusteredMediaStateUseCase: GetClusteredMediaStateUseCase,
) : ViewModel() {
    /** 갤러리 화면 UI 상태 */
    private val _uiState = MutableStateFlow<GalleryUiState>(GalleryUiState.NoneSelected(EMPTY_CLUSTER))
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    private val saveCompletedEventsChannel = Channel<SaveCompletedEvent>(capacity = Channel.BUFFERED)
    val saveCompletedEvents = saveCompletedEventsChannel.receiveAsFlow()

    /** 클러스터 캐시 스냅샷 상태를 Collect하는 코루틴 Job */
    private var clusterStateCollectJob: Job? = null
    private var clusterId: Long? = null
    private var saveJob: Job? = null

    init {
        observeClusterState()
    }

    /**
     * 최초 상태값을 초기화한다.
     *
     * @param cluster 화면에 표시할 클러스터
     */
    fun initialize(cluster: ClusterUiModel) {
        if (_uiState.value.cluster != EMPTY_CLUSTER) return

        clusterId = cluster.id
        _uiState.value = GalleryUiState.NoneSelected(cluster = cluster)
    }

    /**
     * 미디어 선택 상태를 토글한다
     *
     * @param media 토글할 미디어
     */
    fun toggleSelection(media: MediaUiModel) {
        val state = _uiState.value
        if (state is GalleryUiState.Saving) return

        val selected = (state as? GalleryUiState.SomeSelected)?.selectedIds.orEmpty()
        val cluster = state.cluster
        val updatedSelected = if (selected.contains(media.id)) {
            selected - media.id
        } else {
            selected + media.id
        }
        _uiState.value = if (updatedSelected.isEmpty()) {
            GalleryUiState.NoneSelected(cluster)
        } else {
            GalleryUiState.SomeSelected(cluster, updatedSelected)
        }
    }

    /** 모든 미디어를 선택 상태로 만든다 */
    fun selectAll() {
        val state = _uiState.value
        if (state is GalleryUiState.Saving) return
        val cluster = state.cluster
        val updatedSelected = cluster.mediaList.map { it.id }.toSet()
        _uiState.value = if (updatedSelected.isEmpty()) {
            GalleryUiState.NoneSelected(cluster)
        } else {
            GalleryUiState.SomeSelected(cluster, updatedSelected)
        }
    }

    /** 선택된 미디어를 모두 해제한다 */
    fun clearSelection() {
        val state = _uiState.value
        if (state is GalleryUiState.Saving) return
        _uiState.value = GalleryUiState.NoneSelected(state.cluster)
    }

    /** 선택된 미디어를 앨범으로 저장한다 */
    fun saveSelectedMedia() {
        if (saveJob?.isActive == true) return
        val state = _uiState.value
        val selectedIds = (state as? GalleryUiState.SomeSelected)?.selectedIds.orEmpty()
        if (selectedIds.isEmpty()) return

        val savingState = GalleryUiState.Saving(state.cluster, selectedIds)
        _uiState.value = savingState

        saveJob = viewModelScope.launch {
            try {
                val domainCluster = savingState.cluster.toDomain()
                val selectedCluster = domainCluster.copy(
                    mediaList = domainCluster.mediaList.filter { it.id in selectedIds },
                )
                val savedCount = runCatching {
                    val savedMediaList = saveAlbumUseCase(selectedCluster)
                    savedMediaList.size
                }.getOrDefault(0)
                saveCompletedEventsChannel.trySend(
                    SaveCompletedEvent(savingState.cluster.title, savedCount),
                )
            } finally {
                saveJob = null
            }
        }
    }

    /**
     * 캐시 스냅샷을 수집하고 변경이 발생하면 최신 상태로 동기화한다.
     */
    private fun observeClusterState() {
        if (clusterStateCollectJob != null) return

        clusterStateCollectJob = viewModelScope.launch {
            getClusteredMediaStateUseCase().collect { clusters ->
                val updatedCluster = clusters.firstOrNull { it.id == clusterId }?.toUiModel()

                _uiState.update {
                    val newCluster = updatedCluster ?: it.cluster.copy(mediaList = emptyList())
                    GalleryUiState.NoneSelected(newCluster)
                }
            }
        }
    }

    companion object {
        private val EMPTY_CLUSTER = ClusterUiModel(
            id = 0L,
            title = "",
            mediaList = emptyList(),
        )
    }
}
