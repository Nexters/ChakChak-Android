package com.chac.feature.album.clustering

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chac.domain.album.media.usecase.GetClusteredMediaStateUseCase
import com.chac.domain.album.media.usecase.GetClusteredMediaStreamUseCase
import com.chac.domain.album.media.usecase.SaveAlbumUseCase
import com.chac.domain.album.media.usecase.StartClusteringUseCase
import com.chac.feature.album.clustering.model.ClusteringUiState
import com.chac.feature.album.mapper.toDomain
import com.chac.feature.album.mapper.toUiModel
import com.chac.feature.album.model.MediaClusterUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/** 클러스터링 화면 상태를 제공하는 ViewModel */
@HiltViewModel
class ClusteringViewModel @Inject constructor(
    private val getClusteredMediaStreamUseCase: GetClusteredMediaStreamUseCase,
    private val startClusteringUseCase: StartClusteringUseCase,
    private val getClusteredMediaStateUseCase: GetClusteredMediaStateUseCase,
    private val saveAlbumUseCase: SaveAlbumUseCase,
) : ViewModel() {
    /** 클러스터링 화면의 상태 */
    private val _uiState = MutableStateFlow<ClusteringUiState>(ClusteringUiState.PermissionChecking)
    val uiState: StateFlow<ClusteringUiState> = _uiState.asStateFlow()

    /** 앨범 전체 저장 완료 이벤트 채널 */
    private val saveCompletedEventsChannel = Channel<Unit>(capacity = Channel.BUFFERED)
    val saveCompletedEvents = saveCompletedEventsChannel.receiveAsFlow()

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
                _uiState.value = ClusteringUiState.Loading(emptyList())

                // 클러스터 스트림을 수집하며 로딩 상태에 누적한다.
                getClusteredMediaStreamUseCase()
                    .retryWhen { cause, _ ->
                        if (cause is CancellationException) return@retryWhen false

                        // 기존 누적 데이터를 초기화한다.
                        _uiState.value = ClusteringUiState.Loading(emptyList())

                        Timber.e(cause, "Failed to collect cluster stream; retrying")
                        true
                    }
                    .collect { cluster ->
                        val updatedClusters = currentClusters() + cluster.toUiModel()
                        _uiState.value = ClusteringUiState.Loading(mergeThumbnails(updatedClusters))
                    }

                // 클러스터링이 완료 되면 Completed 상태로 변경
                _uiState.value = ClusteringUiState.Completed(currentClusters())
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
                    // 초기 스트림 수집 중에는 중복 갱신을 피한다.
                    if (clusterCollectJob != null) return@collect

                    val uiClusters = mergeThumbnails(clusters.map { it.toUiModel() })
                    if (_uiState.value is ClusteringUiState.WithClusters) {
                        _uiState.value = ClusteringUiState.Completed(uiClusters)
                    }
                }
        }
    }

    /** 클러스터 전체를 앨범으로 저장한다. */
    fun onClickSaveAll(cluster: MediaClusterUiModel) {
        viewModelScope.launch {
            runCatching { saveAlbumUseCase(cluster.toDomain()) }
                .onSuccess { saveCompletedEventsChannel.trySend(Unit) }
                .onFailure { t -> Timber.e(t, "Failed to save cluster album") }
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

    /**
     * 저장 이후 mediaList가 비어도 썸네일이 유지되도록 이전 썸네일을 병합한다.
     *
     * @param newClusters 최신 클러스터 목록
     * @return 썸네일이 보존된 클러스터 목록
     */
    private fun mergeThumbnails(
        newClusters: List<MediaClusterUiModel>,
    ): List<MediaClusterUiModel> {
        val previousThumbnails = currentClusters().associateBy(
            keySelector = { it.id },
            valueTransform = { it.thumbnailUriStrings },
        )

        return newClusters.map { cluster ->
            if (cluster.thumbnailUriStrings.isNotEmpty()) {
                cluster
            } else {
                val fallback = previousThumbnails[cluster.id]
                if (fallback != null) cluster.copy(thumbnailUriStrings = fallback) else cluster
            }
        }
    }
}
