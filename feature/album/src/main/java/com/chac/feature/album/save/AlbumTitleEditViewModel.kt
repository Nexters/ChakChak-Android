package com.chac.feature.album.save

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chac.domain.album.media.model.MediaCluster
import com.chac.domain.album.media.usecase.GetAllMediaStateUseCase
import com.chac.domain.album.media.usecase.GetClusteredMediaStateUseCase
import com.chac.domain.album.media.usecase.SaveAlbumUseCase
import com.chac.feature.album.gallery.model.SaveCompletedEvent
import com.chac.feature.album.save.model.AlbumTitleEditUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 앨범명 수정 화면 ViewModel.
 *
 * 역할:
 * - 초기 선택 목록을 구성한다(특정 [MediaCluster] 기반 또는 전체 미디어에서 선택).
 * - UI 입력에 맞춰 현재 제목 상태를 유지한다.
 * - 앨범 저장을 트리거하고 1회성 저장 완료 이벤트를 발행한다.
 *
 * 참고:
 * - [initialize]는 1회 초기화만 수행하도록 설계되어, 이미 초기화된 이후의 재호출은 무시한다.
 * - 저장은 동시 요청을 막기 위해 Job으로 가드한다.
 */
@HiltViewModel
class AlbumTitleEditViewModel @Inject constructor(
    private val saveAlbumUseCase: SaveAlbumUseCase,
    private val getClusteredMediaStateUseCase: GetClusteredMediaStateUseCase,
    private val getAllMediaStateUseCase: GetAllMediaStateUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AlbumTitleEditUiState())

    /** Compose에서 관찰하는 UI 상태. */
    val uiState: StateFlow<AlbumTitleEditUiState> = _uiState.asStateFlow()

    /**
     * 저장 성공 이후 1회성으로 발행되는 이벤트 스트림.
     *
     * UI 수집기가 아직 준비되지 않은 순간에도 이벤트가 유실되지 않도록 버퍼를 둔다.
     */
    private val saveCompletedEventsChannel = Channel<SaveCompletedEvent>(capacity = Channel.BUFFERED)
    val saveCompletedEvents = saveCompletedEventsChannel.receiveAsFlow()

    private var initializeJob: Job? = null
    private var saveJob: Job? = null

    private var selectedCluster: MediaCluster? = null

    /**
     * 화면 상태를 1회 초기화한다.
     *
     * @param clusterId 저장 대상 클러스터 ID. null이 아니면 해당 클러스터를 조회한 뒤,
     * [selectedMediaIds]에 포함된 항목만 클러스터에서 선별한다.
     * @param selectedMediaIds 호출자가 전달한 선택 미디어 ID 목록.
     * 클러스터 기반 선별이 실패/비어있을 때는 "전체 미디어"에서 이 순서를 유지해 매핑한다.
     */
    fun initialize(clusterId: Long?, selectedMediaIds: List<Long>) {
        if (initializeJob?.isActive == true) return
        if (_uiState.value.isInitialized) return

        initializeJob = viewModelScope.launch {
            try {
                val selectedIds = selectedMediaIds.toHashSet()
                val cluster = if (clusterId != null) {
                    val clusters = getClusteredMediaStateUseCase().first()
                    clusters.firstOrNull { it.id == clusterId }
                } else {
                    null
                }
                // 클러스터가 있으면 우선 클러스터에서 선택을 구성한다.
                // (전체 미디어를 조회/스캔하는 비용을 줄일 수 있음)
                val clusterMedia = cluster?.mediaList?.filter { it.id in selectedIds }.orEmpty()

                // 클러스터에서 선택을 만들지 못했거나 결과가 비어있으면 "전체 미디어"에서 선택을 구성한다.
                val allMedia = if (clusterMedia.isEmpty()) {
                    getAllMediaStateUseCase().first()
                } else {
                    emptyList()
                }
                val allMediaById = allMedia.associateBy { it.id }
                val allMediaSelected = selectedMediaIds.mapNotNull { id -> allMediaById[id] }

                val mediaList = clusterMedia.ifEmpty { allMediaSelected }
                // 기본 제목은 클러스터 주소로 만든다.
                val defaultTitle = cluster?.address.orEmpty().trim()

                if (mediaList.isEmpty()) {
                    // 저장할 대상이 없다. 로딩 상태에서 빠져나올 수 있도록 초기화 완료로만 표시한다.
                    _uiState.update { it.copy(isInitialized = true) }
                    return@launch
                }

                selectedCluster = MediaCluster(
                    id = clusterId ?: 0L,
                    mediaList = mediaList,
                    address = cluster?.address.orEmpty(),
                    formattedDate = cluster?.formattedDate.orEmpty(),
                )

                _uiState.value = AlbumTitleEditUiState(
                    isInitialized = true,
                    title = TextFieldValue(""),
                    placeholder = defaultTitle,
                    selectedCount = mediaList.size,
                    selectedUriStrings = mediaList.map { it.uriString },
                    isSaving = false,
                )
            } finally {
                initializeJob = null
            }
        }
    }

    /** 사용자의 텍스트 입력 변경을 UI 상태에 반영한다. */
    fun updateTitle(value: TextFieldValue) {
        _uiState.update { state ->
            state.copy(title = value)
        }
    }

    /** 제목 입력값을 비운다. */
    fun clearTitle() {
        _uiState.update { state ->
            state.copy(title = TextFieldValue(""))
        }
    }

    /**
     * 현재 선택/제목으로 앨범을 저장한다.
     *
     * 최소 1개 이상 저장된 경우에만 [SaveCompletedEvent]를 발행한다.
     * 실패하거나 저장된 항목이 0개면 UI를 비저장 상태로 되돌린다.
     */
    fun save() {
        if (saveJob?.isActive == true) return
        val cluster = selectedCluster ?: return

        saveJob = viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val albumTitle = sanitizeAlbumTitle(_uiState.value.title.text)
                val savedCount = runCatching {
                    saveAlbumUseCase(cluster, albumTitle).size
                }.getOrElse { e ->
                    Timber.e(e, "Failed to save album")
                    0
                }

                if (savedCount > 0) {
                    saveCompletedEventsChannel.trySend(
                        SaveCompletedEvent(albumTitle, savedCount),
                    )
                } else {
                    _uiState.update { it.copy(isSaving = false) }
                }
            } finally {
                saveJob = null
            }
        }
    }

    /**
     * 사용자가 입력한 제목을 저장/표시에 적합한 값으로 정리한다.
     *
     * 규칙:
     * - 공백/빈 문자열이면 기본값으로 대체한다.
     * - `/`는 하위 저장소에서 경로처럼 해석될 여지를 줄이기 위해 `_`로 치환한다.
     */
    private fun sanitizeAlbumTitle(title: String): String {
        val trimmed = title.trim()
        if (trimmed.isBlank()) return "Chac"
        return trimmed.replace("/", "_")
    }
}
