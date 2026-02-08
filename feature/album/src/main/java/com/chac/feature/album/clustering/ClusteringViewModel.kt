package com.chac.feature.album.clustering

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chac.domain.album.media.usecase.GetClusteredMediaStateUseCase
import com.chac.domain.album.media.usecase.GetClusteredMediaStreamUseCase
import com.chac.domain.album.media.usecase.GetAllMediaCountUseCase
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/** 클러스터링 화면 상태를 제공하는 ViewModel */
@HiltViewModel
class ClusteringViewModel @Inject constructor(
    private val getClusteredMediaStreamUseCase: GetClusteredMediaStreamUseCase,
    private val startClusteringUseCase: StartClusteringUseCase,
    private val getClusteredMediaStateUseCase: GetClusteredMediaStateUseCase,
    private val getAllMediaCountUseCase: GetAllMediaCountUseCase,
) : ViewModel() {
    /** 클러스터링 화면의 상태 */
    private val _uiState = MutableStateFlow<ClusteringUiState>(ClusteringUiState.PermissionChecking)
    val uiState: StateFlow<ClusteringUiState> = _uiState.asStateFlow()

    /**
     * 클러스터 스트림 수집의 예외 처리를 위한 Job
     *
     * 1. 클러스터 스트림 중복 수집 방지
     * 2. 수집이 진행중일 때 권한이 거절되면 캔슬
     */
    private var clusterCollectJob: Job? = null

    /** 캐시 스냅샷 수집 Job */
    private var clusterStateCollectJob: Job? = null

    /**
     * 권한 변경 결과를 반영해 UI 상태와 스트림 수집을 갱신한다.
     *
     * @param hasPermission 권한 허용 여부
     */
    fun onPermissionChanged(hasPermission: Boolean) {
        if (!hasPermission) {
            clusterCollectJob?.cancel()
            clusterCollectJob = null
            clusterStateCollectJob?.cancel()
            clusterStateCollectJob = null
            _uiState.value = ClusteringUiState.PermissionDenied
            return
        }

        // 권한이 이미 허용된 상태라면 수집을 다시 시작하지 않는다.
        if (_uiState.value is ClusteringUiState.WithClusters) return
        if (clusterCollectJob != null) return
        if (clusterStateCollectJob == null) {
            observeClusterState()
        }

        initializeClusters()
    }

    /**
     * 초기 클러스터 스트림 수집을 수집하고 상태를 갱신한다.
     */
    private fun initializeClusters() {
        // TODO 나중에 여기서 클러스터링 실행 시도만 하고, 클러스터링 후 데이터를 넣어주고 가져온느 작업을 Worker에서 추가해줘야함
        // startClusteringUseCase()
        clusterCollectJob = viewModelScope.launch {
            try {
                _uiState.value = ClusteringUiState.Loading(
                    clusters = emptyList(),
                    allPhotosCount = 0,
                )

                val allPhotosCountDeferred = async {
                    runCatching { getAllMediaCountUseCase() }.getOrElse { 0 }
                }

                launch {
                    val count = allPhotosCountDeferred.await()
                    if (count <= 0) return@launch

                    // 클러스터 수집 진행 여부와 무관하게 "모든 사진" 카운트를 먼저 확정한다.
                    _uiState.value = when (val current = _uiState.value) {
                        is ClusteringUiState.Loading -> current.copy(allPhotosCount = count)
                        is ClusteringUiState.Completed -> current.copy(allPhotosCount = count)
                        else -> current
                    }
                }

                // 클러스터 스트림을 수집하며 로딩 상태에 누적한다.
                getClusteredMediaStreamUseCase()
                    .retryWhen { cause, _ ->
                        if (cause is CancellationException) return@retryWhen false

                        // 기존 누적 데이터를 초기화한다.
                        _uiState.value = ClusteringUiState.Loading(
                            clusters = emptyList(),
                            allPhotosCount = currentAllPhotosCount(),
                        )

                        Timber.e(cause, "Failed to collect cluster stream; retrying")
                        true
                    }
                    .collect { cluster ->
                        // 타겟 전체 카운트를 한 번 계산한 뒤엔 계속 유지한다.
                        val allPhotosCount = currentAllPhotosCount().takeIf { it > 0 }
                            ?: allPhotosCountDeferred.await()
                        val updatedClusters = currentClusters() + cluster.toUiModel()
                        _uiState.value = ClusteringUiState.Loading(
                            clusters = mergeThumbnails(updatedClusters),
                            allPhotosCount = allPhotosCount,
                        )
                    }

                // 클러스터링 중 저장이 발생했을 수 있으므로 최신 상태를 반영한다.
                val latestClusters = getClusteredMediaStateUseCase().first()
                val uiClusters = mergeThumbnails(latestClusters.map { it.toUiModel() })
                val allPhotosCount = currentAllPhotosCount().takeIf { it > 0 }
                    ?: allPhotosCountDeferred.await()

                _uiState.value = ClusteringUiState.Completed(
                    clusters = uiClusters,
                    allPhotosCount = allPhotosCount,
                )
            } finally {
                clusterCollectJob = null
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
                    // 현재 상태 타입(Loading/Completed)을 유지하면서 클러스터 목록만 갱신한다.
                    _uiState.value = when (_uiState.value) {
                        is ClusteringUiState.Loading -> ClusteringUiState.Loading(
                            clusters = uiClusters,
                            allPhotosCount = currentAllPhotosCount(),
                        )
                        is ClusteringUiState.Completed -> ClusteringUiState.Completed(
                            clusters = uiClusters,
                            allPhotosCount = currentAllPhotosCount(),
                        )
                        else -> return@collect
                    }
                }
        }
    }

    /**
     * 현재 UI 상태에 포함된 클러스터 목록을 가져온다.
     */
    private fun currentClusters(): List<MediaClusterUiModel> = when (val state = _uiState.value) {
        is ClusteringUiState.WithClusters -> state.clusters
        ClusteringUiState.PermissionChecking -> emptyList()
        ClusteringUiState.PermissionDenied -> emptyList()
    }

    private fun currentAllPhotosCount(): Int = when (val state = _uiState.value) {
        is ClusteringUiState.WithClusters -> state.allPhotosCount
        else -> 0
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
        val previousClusters = currentClusters().associateBy { it.id }

        return newClusters.map { cluster ->
            val previous = previousClusters[cluster.id]
            val mergedThumbnails = cluster.thumbnailUriStrings.ifEmpty {
                previous?.thumbnailUriStrings.orEmpty()
            }
            cluster.copy(thumbnailUriStrings = mergedThumbnails)
        }
    }
}
