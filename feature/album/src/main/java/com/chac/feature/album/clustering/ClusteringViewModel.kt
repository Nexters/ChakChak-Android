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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.retryWhen
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
    /** 클러스터링 화면의 상태 */
    private val _uiState = MutableStateFlow<ClusteringUiState>(ClusteringUiState.PermissionChecking)
    val uiState: StateFlow<ClusteringUiState> = _uiState.asStateFlow()

    /** 캐시 스냅샷 수집 Job */
    private var clusterStateCollectJob: Job? = null

    /** WorkManager 상태 수집 Job */
    private var clusteringWorkStateCollectJob: Job? = null

    /** 최근 클러스터 스냅샷 */
    private var latestClusters: List<MediaClusterUiModel> = emptyList()

    /** 최근 전체 사진 개수 */
    private var latestTotalPhotoCount: Int = 0

    init {
        viewModelScope.launch {
            getAllMediaStateUseCase().collect { mediaList ->
                val count = mediaList.size
                latestTotalPhotoCount = count

                // UI가 WithClusters 상태일 때만 카운트를 반영한다.
                _uiState.update { state ->
                    when (state) {
                        is ClusteringUiState.Loading -> state.copy(totalPhotoCount = count)
                        is ClusteringUiState.Completed -> state.copy(totalPhotoCount = count)
                        else -> state
                    }
                }
            }
        }
    }

    /**
     * 미디어, 위치 권한 변경 결과를 반영해 UI 상태와 스트림 수집을 갱신한다.
     *
     * @param hasPermission 권한 허용 여부
     */
    fun onMediaWithLocationPermissionChanged(hasPermission: Boolean) {
        if (!hasPermission) {
            clusterStateCollectJob?.cancel()
            clusterStateCollectJob = null
            clusteringWorkStateCollectJob?.cancel()
            clusteringWorkStateCollectJob = null
            cancelClusteringUseCase()
            _uiState.value = ClusteringUiState.PermissionDenied
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
                    val uiClusters = mergeThumbnails(clusters.map { it.toUiModel() })
                    latestClusters = uiClusters
                    // 현재 상태 타입(Loading/Completed)을 유지하면서 클러스터 목록만 갱신한다.
                    _uiState.update { current ->
                        when (current) {
                            is ClusteringUiState.Loading -> current.copy(clusters = uiClusters)
                            is ClusteringUiState.Completed -> current.copy(clusters = uiClusters)
                            else -> current
                        }
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
                    when (state) {
                        ClusteringWorkState.Idle -> {
                            if (latestClusters.isNotEmpty()) {
                                _uiState.value = ClusteringUiState.Completed(
                                    totalPhotoCount = currentTotalPhotoCount(),
                                    clusters = latestClusters,
                                )
                            } else if (_uiState.value != ClusteringUiState.PermissionDenied) {
                                _uiState.value = ClusteringUiState.PermissionChecking
                            }
                        }

                        ClusteringWorkState.Enqueued,
                        ClusteringWorkState.Running,
                        -> {
                            _uiState.value = ClusteringUiState.Loading(
                                totalPhotoCount = currentTotalPhotoCount(),
                                clusters = latestClusters,
                            )
                        }

                        ClusteringWorkState.Succeeded -> {
                            _uiState.value = ClusteringUiState.Completed(
                                totalPhotoCount = currentTotalPhotoCount(),
                                clusters = latestClusters,
                            )
                        }

                        ClusteringWorkState.Failed,
                        ClusteringWorkState.Cancelled,
                        -> {
                            Timber.w("Clustering work finished with state=$state")
                            _uiState.value = ClusteringUiState.Completed(
                                totalPhotoCount = currentTotalPhotoCount(),
                                clusters = latestClusters,
                            )
                        }
                    }
                }
        }
    }

    /**
     * 현재 UI 상태에 포함된 전체 사진 개수를 가져온다.
     */
    private fun currentTotalPhotoCount(): Int = when (val state = _uiState.value) {
        is ClusteringUiState.WithClusters -> state.totalPhotoCount
        ClusteringUiState.PermissionChecking -> latestTotalPhotoCount
        ClusteringUiState.PermissionDenied -> latestTotalPhotoCount
    }

    /**
     * 클러스터 갱신 시 썸네일을 보존하도록 이전 상태를 병합한다.
     *
     * @param newClusters 최신 클러스터 목록
     * @return 썸네일이 보존된 클러스터 목록
     */
    private fun mergeThumbnails(
        newClusters: List<MediaClusterUiModel>,
    ): List<MediaClusterUiModel> {
        val previousClusters = latestClusters.associateBy { it.id }

        return newClusters.map { cluster ->
            val previous = previousClusters[cluster.id]
            val mergedThumbnails = cluster.thumbnailUriStrings.ifEmpty {
                previous?.thumbnailUriStrings.orEmpty()
            }
            cluster.copy(thumbnailUriStrings = mergedThumbnails)
        }
    }
}
