package com.chac.feature.album.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chac.domain.album.media.model.ClusteringWorkState
import com.chac.domain.album.media.usecase.CancelClusteringUseCase
import com.chac.domain.album.media.usecase.GetAllMediaStateUseCase
import com.chac.domain.album.media.usecase.GetClusteredMediaStateUseCase
import com.chac.domain.album.media.usecase.ObserveClusteringWorkStateUseCase
import com.chac.domain.album.media.usecase.StartClusteringUseCase
import com.chac.feature.album.clustering.model.ClusteringUiState
import com.chac.feature.album.mapper.toUiModel
import com.chac.feature.album.model.MediaClusterUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class AiOrganizeViewModel @Inject constructor(
    private val observeClusteringWorkStateUseCase: ObserveClusteringWorkStateUseCase,
    private val cancelClusteringUseCase: CancelClusteringUseCase,
    private val startClusteringUseCase: StartClusteringUseCase,
    private val getClusteredMediaStateUseCase: GetClusteredMediaStateUseCase,
    private val getAllMediaStateUseCase: GetAllMediaStateUseCase,
) : ViewModel() {
    private val hasMediaWithLocationPermission = MutableStateFlow<Boolean?>(null)
    private val clustersState = MutableStateFlow<List<MediaClusterUiModel>>(emptyList())
    private val workState = MutableStateFlow(ClusteringWorkState.Idle)
    private val totalPhotoCountState = MutableStateFlow(0)

    private val _promptTextState = MutableStateFlow("")
    val promptTextState: StateFlow<String> = _promptTextState

    private val _hasRequestedPromptState = MutableStateFlow(false)
    val hasRequestedPromptState: StateFlow<Boolean> = _hasRequestedPromptState

    val uiState: StateFlow<ClusteringUiState> = combine(
        hasMediaWithLocationPermission,
        workState,
        clustersState,
        totalPhotoCountState,
    ) { hasPermission, currentWorkState, clusters, totalPhotoCount ->
        when (hasPermission) {
            null -> ClusteringUiState.PermissionChecking
            false -> ClusteringUiState.PermissionDenied
            true -> when (currentWorkState) {
                ClusteringWorkState.Enqueued,
                ClusteringWorkState.Running,
                -> ClusteringUiState.Loading(
                    totalPhotoCount = totalPhotoCount,
                    clusters = clusters,
                )

                ClusteringWorkState.Succeeded,
                ClusteringWorkState.Failed,
                ClusteringWorkState.Cancelled,
                ClusteringWorkState.Idle,
                -> ClusteringUiState.Completed(
                    totalPhotoCount = totalPhotoCount,
                    clusters = clusters,
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ClusteringUiState.PermissionChecking,
    )

    private var clusterStateCollectJob: Job? = null
    private var clusteringWorkStateCollectJob: Job? = null

    init {
        viewModelScope.launch {
            getAllMediaStateUseCase().collect { mediaList ->
                totalPhotoCountState.value = mediaList.size
            }
        }
    }

    fun onPromptChanged(promptText: String) {
        _promptTextState.value = promptText
    }

    fun onPromptChipClicked(prompt: String) {
        _promptTextState.value = prompt
    }

    fun onMediaWithLocationPermissionChanged(hasPermission: Boolean) {
        hasMediaWithLocationPermission.value = hasPermission
        if (!hasPermission) {
            clusterStateCollectJob?.cancel()
            clusterStateCollectJob = null
            clusteringWorkStateCollectJob?.cancel()
            clusteringWorkStateCollectJob = null
            cancelClusteringUseCase()
            return
        }

        if (clusterStateCollectJob == null) {
            observeClusterState()
        }
        if (clusteringWorkStateCollectJob == null) {
            observeClusteringWorkState()
        }
    }

    fun onRunPromptClustering() {
        val prompt = promptTextState.value.trim()
        if (prompt.isBlank()) return
        if (hasMediaWithLocationPermission.value != true) return

        _hasRequestedPromptState.value = true
        clustersState.value = emptyList()
        workState.value = ClusteringWorkState.Enqueued

        viewModelScope.launch {
            cancelClusteringUseCase()
            startClusteringUseCase(prompt)
        }
    }

    private fun observeClusterState() {
        clusterStateCollectJob = viewModelScope.launch {
            getClusteredMediaStateUseCase()
                .retryWhen { cause, _ ->
                    if (cause is CancellationException) return@retryWhen false

                    Timber.e(cause, "Failed to collect AI cluster state; retrying")
                    true
                }
                .collect { clusters ->
                    if (!hasRequestedPromptState.value) return@collect

                    val newClusters = clusters.map { it.toUiModel() }
                    clustersState.update { previous ->
                        mergeThumbnails(
                            previousClusters = previous,
                            newClusters = newClusters,
                        )
                    }
                }
        }
    }

    private fun observeClusteringWorkState() {
        clusteringWorkStateCollectJob = viewModelScope.launch {
            observeClusteringWorkStateUseCase()
                .retryWhen { cause, _ ->
                    if (cause is CancellationException) return@retryWhen false

                    Timber.e(cause, "Failed to collect AI clustering work state; retrying")
                    true
                }
                .collect { state ->
                    if (!hasRequestedPromptState.value) return@collect
                    workState.value = state
                }
        }
    }

    private fun mergeThumbnails(
        previousClusters: List<MediaClusterUiModel>,
        newClusters: List<MediaClusterUiModel>,
    ): List<MediaClusterUiModel> {
        val previousClustersById = previousClusters.associateBy { it.id }

        return newClusters.map { cluster ->
            val previous = previousClustersById[cluster.id]
            val mergedThumbnails = cluster.thumbnailUriStrings.ifEmpty {
                previous?.thumbnailUriStrings.orEmpty()
            }
            cluster.copy(thumbnailUriStrings = mergedThumbnails)
        }
    }
}
