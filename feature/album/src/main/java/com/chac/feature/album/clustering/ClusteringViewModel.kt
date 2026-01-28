package com.chac.feature.album.clustering

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chac.domain.album.media.GetClusteredMediaStateUseCase
import com.chac.domain.album.media.GetClusteredMediaStreamUseCase
import com.chac.domain.album.media.SaveAlbumUseCase
import com.chac.feature.album.clustering.model.ClusteringUiState
import com.chac.feature.album.clustering.model.toUiModel
import com.chac.feature.album.model.ClusterUiModel
import com.chac.feature.album.model.toDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 클러스터링 화면 상태를 제공하는 ViewModel */
@HiltViewModel
class ClusteringViewModel @Inject constructor(
    private val getClusteredMediaStreamUseCase: GetClusteredMediaStreamUseCase,
    private val getClusteredMediaStateUseCase: GetClusteredMediaStateUseCase,
    private val saveAlbumUseCase: SaveAlbumUseCase,
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
        clusterCollectJob = viewModelScope.launch {
            try {
                _uiState.value = ClusteringUiState.Loading(emptyList())

                // 클러스터 스트림을 수집하며 로딩 상태에 누적한다.
                getClusteredMediaStreamUseCase().collect { cluster ->
                    val updatedClusters = currentClusters() + cluster.toUiModel()
                    _uiState.value = ClusteringUiState.Loading(updatedClusters)
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
            getClusteredMediaStateUseCase().collect { clusters ->
                // 초기 스트림 수집 중에는 중복 갱신을 피한다.
                if (clusterCollectJob != null) return@collect

                val uiClusters = clusters.map { it.toUiModel() }
                if (_uiState.value is ClusteringUiState.WithClusters) {
                    _uiState.value = ClusteringUiState.Completed(uiClusters)
                }
            }
        }
    }

    /** 클러스터 전체를 앨범으로 저장한다. */
    fun onClickSaveAll(cluster: ClusterUiModel) {
        viewModelScope.launch {
            saveAlbumUseCase(cluster.toDomain())
        }
    }

    /**
     * 현재 UI 상태에 포함된 클러스터 목록을 가져온다.
     */
    private fun currentClusters(): List<ClusterUiModel> = when (val state = _uiState.value) {
        is ClusteringUiState.WithClusters -> state.clusters
        ClusteringUiState.PermissionChecking -> emptyList()
        ClusteringUiState.PermissionDenied -> emptyList()
    }
}
