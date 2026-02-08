package com.chac.feature.album.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chac.domain.album.media.usecase.GetClusteredMediaStateUseCase
import com.chac.domain.album.media.usecase.SaveAlbumUseCase
import com.chac.feature.album.gallery.model.GalleryUiState
import com.chac.feature.album.gallery.model.SaveCompletedEvent
import com.chac.feature.album.mapper.toDomain
import com.chac.feature.album.mapper.toUiModel
import com.chac.feature.album.model.MediaClusterUiModel
import com.chac.feature.album.model.MediaUiModel

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

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
     * @param clusterId 화면에 표시할 클러스터 ID
     */
    fun initialize(clusterId: Long) {
        if (this.clusterId != null) return

        this.clusterId = clusterId

        // 현재 스냅샷에 이미 값이 있으면 즉시 세팅하고, 이후 변경은 observeClusterState()가 동기화한다.
        viewModelScope.launch {
            val clusters = getClusteredMediaStateUseCase().first()
            val cluster = clusters.firstOrNull { it.id == clusterId }?.toUiModel() ?: return@launch
            _uiState.value = GalleryUiState.NoneSelected(cluster = cluster)
        }
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
                val result = runCatching {
                    val savedMediaList = saveAlbumUseCase(selectedCluster)
                    savedMediaList.size
                }
                if (result.isSuccess) {
                    saveCompletedEventsChannel.trySend(
                        SaveCompletedEvent(savingState.cluster.address, result.getOrNull() ?: 0),
                    )
                } else {
                    _uiState.value = GalleryUiState.SomeSelected(savingState.cluster, selectedIds)
                    Timber.e(result.exceptionOrNull(), "Failed to save selected media")
                }
            } finally {
                saveJob = null
            }
        }
    }

    /**
     * 현재 선택된 미디어 목록을 반환한다.
     */
    fun getSelectedMediaList(): List<MediaUiModel> {
        val state = _uiState.value

        val selectedIds = when (state) {
            is GalleryUiState.SomeSelected -> state.selectedIds
            is GalleryUiState.Saving -> state.selectedIds
            else -> emptySet()
        }
        if (selectedIds.isEmpty()) return emptyList()
        return state.cluster.mediaList.filter { it.id in selectedIds }
    }

    /**
     * 캐시 스냅샷을 수집하고 변경이 발생하면 최신 상태로 동기화한다.
     */
    private fun observeClusterState() {
        if (clusterStateCollectJob != null) return

        clusterStateCollectJob = viewModelScope.launch {
            getClusteredMediaStateUseCase()
                .retryWhen { cause, _ ->
                    if (cause is CancellationException) return@retryWhen false

                    Timber.e(cause, "Failed to collect cluster state; retrying")
                    true
                }
                .collect { clusters ->
                    val id = clusterId ?: return@collect
                    val updatedCluster = clusters.firstOrNull { it.id == id }?.toUiModel()

                    _uiState.update { current ->
                        val newCluster = when {
                            updatedCluster == null -> current.cluster.copy(mediaList = emptyList())
                            updatedCluster.thumbnailUriStrings.isEmpty() && current.cluster.thumbnailUriStrings.isNotEmpty() ->
                                updatedCluster.copy(
                                    thumbnailUriStrings = current.cluster.thumbnailUriStrings,
                                )
                            else -> updatedCluster
                        }
                        // 현재 상태 타입과 선택 상태를 유지하면서 클러스터만 갱신한다.
                        when (current) {
                            is GalleryUiState.NoneSelected -> GalleryUiState.NoneSelected(newCluster)
                            is GalleryUiState.SomeSelected -> {
                                val validIds = newCluster.mediaList.map { it.id }.toSet()
                                val kept = current.selectedIds.intersect(validIds)
                                if (kept.isEmpty()) {
                                    GalleryUiState.NoneSelected(newCluster)
                                } else {
                                    GalleryUiState.SomeSelected(newCluster, kept)
                                }
                            }
                            is GalleryUiState.Saving -> GalleryUiState.Saving(newCluster, current.selectedIds)
                        }
                    }
                }
        }
    }

    companion object {
        private val EMPTY_CLUSTER = MediaClusterUiModel(
            id = 0L,
            address = "",
            formattedDate = "",
            mediaList = emptyList(),
            thumbnailUriStrings = emptyList(),
        )
    }
}
