package com.chac.feature.album.clustering

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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/** 클러스터링 화면 상태를 제공하는 ViewModel */
@HiltViewModel
class ClusteringViewModel @Inject constructor(
    private val observeClusteringWorkStateUseCase: ObserveClusteringWorkStateUseCase,
    private val cancelClusteringUseCase: CancelClusteringUseCase,
    private val startClusteringUseCase: StartClusteringUseCase,
    private val getClusteredMediaStateUseCase: GetClusteredMediaStateUseCase,
    private val getAllMediaStateUseCase: GetAllMediaStateUseCase,
) : ViewModel() {
    /** 미디어/위치 권한 상태 */
    private val hasMediaWithLocationPermission = MutableStateFlow<Boolean?>(null)

    /** 클러스터 스냅샷 상태 */
    private val clustersState = MutableStateFlow<List<MediaClusterUiModel>>(emptyList())

    /** WorkManager 상태 */
    private val workState = MutableStateFlow(ClusteringWorkState.Idle)

    /** 전체 사진 개수 상태 */
    private val totalPhotoCountState = MutableStateFlow(0)

    /** 클러스터링 화면의 상태 */
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

    /** 캐시 스냅샷 수집 Job */
    private var clusterStateCollectJob: Job? = null

    /** WorkManager 상태 수집 Job */
    private var clusteringWorkStateCollectJob: Job? = null

    init {
        viewModelScope.launch {
            getAllMediaStateUseCase().collect { mediaList ->
                totalPhotoCountState.value = mediaList.size
            }
        }
    }

    /**
     * 미디어, 위치 권한 변경 결과를 반영해 UI 상태와 스트림 수집을 갱신한다.
     *
     * @param hasPermission 권한 허용 여부
     */
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

        requestClusteringIfNeeded()
    }

    /**
     * 캐시가 비어있을 때만 WorkManager에 클러스터링 실행을 요청한다.
     */
    private fun requestClusteringIfNeeded() {
        viewModelScope.launch {
            val hasClusters = getClusteredMediaStateUseCase().first().isNotEmpty()
            if (!hasClusters) {
                startClusteringUseCase()
            }
        }
    }

    /**
     * 캐시 스냅샷을 수집해 저장 이후 최신 상태로 UI를 동기화한다.
     */
    private fun observeClusterState() {
        clusterStateCollectJob = viewModelScope.launch {
            getClusteredMediaStateUseCase()
                .retryWhen { cause, _ ->
                    if (cause is CancellationException) return@retryWhen false

                    Timber.e(cause, "Failed to collect cluster state; retrying")
                    true
                }
                .collect { clusters ->
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

    /**
     * WorkManager 상태를 수집해 로딩/완료 상태를 갱신한다.
     */
    private fun observeClusteringWorkState() {
        clusteringWorkStateCollectJob = viewModelScope.launch {
            observeClusteringWorkStateUseCase()
                .retryWhen { cause, _ ->
                    if (cause is CancellationException) return@retryWhen false

                    Timber.e(cause, "Failed to collect clustering work state; retrying")
                    true
                }
                .collect { state ->
                    if (state == ClusteringWorkState.Failed || state == ClusteringWorkState.Cancelled) {
                        Timber.w("Clustering work finished with state=$state")
                    }
                    workState.value = state
                }
        }
    }

    /**
     * 클러스터 갱신 시 썸네일을 보존하도록 이전 상태를 병합한다.
     *
     * @param previousClusters 이전 클러스터 목록
     * @param newClusters 최신 클러스터 목록
     * @return 썸네일이 보존된 클러스터 목록
     */
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
