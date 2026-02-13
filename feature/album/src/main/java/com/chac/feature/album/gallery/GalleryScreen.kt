package com.chac.feature.album.gallery

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chac.core.designsystem.ui.component.ChacTopBar
import com.chac.core.designsystem.ui.modifier.verticalScrollFadingEdge
import com.chac.core.designsystem.ui.theme.ChacColors
import com.chac.core.designsystem.ui.theme.ChacTextStyles
import com.chac.core.designsystem.ui.theme.ChacTheme
import com.chac.core.resources.R
import com.chac.domain.album.media.model.MediaType
import com.chac.feature.album.gallery.model.GalleryUiState
import com.chac.feature.album.model.MediaClusterUiModel
import com.chac.feature.album.model.MediaUiModel

/**
 * 갤러리 화면 라우트
 *
 * @param clusterId 화면에 표시할 클러스터 ID
 * @param viewModel 갤러리 화면의 뷰모델
 * @param onClickNext '다음' 버튼 클릭 이벤트 콜백 (selectedMediaIds)
 * @param onLongClickMediaItem 미디어 아이템의 롱클릭 이벤트 콜백
 * @param onClickBack 뒤로가기 버튼 클릭 이벤트 콜백
 */
@Composable
fun GalleryRoute(
    clusterId: Long,
    viewModel: GalleryViewModel = hiltViewModel(),
    onClickNext: (List<Long>) -> Unit,
    onLongClickMediaItem: (Long?, Long) -> Unit,
    onClickBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cluster = uiState.cluster
    val title = cluster.address.ifBlank {
        cluster.formattedDate.ifBlank { stringResource(R.string.clustering_default_album_title) }
    }

    LaunchedEffect(viewModel, clusterId) {
        viewModel.initialize(clusterId)
    }

    GalleryScreen(
        uiState = uiState,
        title = title,
        onToggleMedia = viewModel::toggleSelection,
        onClickSelectAll = { selected: Boolean ->
            if (selected) {
                viewModel.selectAll()
            } else {
                viewModel.clearSelection()
            }
        },
        onClickSave = {
            val selectedIds = viewModel.getSelectedMediaIds()
            if (selectedIds.isEmpty()) return@GalleryScreen
            onClickNext(selectedIds)
        },
        onLongClickMediaItem = { mediaId ->
            onLongClickMediaItem(clusterId, mediaId)
        },
        onClickBack = onClickBack,
    )
}

/**
 * 전체 사진 갤러리 화면 라우트
 *
 * @param viewModel 갤러리 화면의 뷰모델
 * @param onClickNext '다음' 버튼 클릭 이벤트 콜백 (selectedMediaIds)
 * @param onLongClickMediaItem 미디어 아이템의 롱클릭 이벤트 콜백
 * @param onClickBack 뒤로가기 버튼 클릭 이벤트 콜백
 */
@Composable
fun AllPhotosGalleryRoute(
    viewModel: GalleryViewModel = hiltViewModel(),
    onClickNext: (List<Long>) -> Unit,
    onLongClickMediaItem: (Long?, Long) -> Unit,
    onClickBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.initializeAllPhotos()
    }

    GalleryScreen(
        uiState = uiState,
        title = stringResource(R.string.clustering_total_photo_title),
        onToggleMedia = viewModel::toggleSelection,
        onClickSelectAll = { selected: Boolean ->
            if (selected) {
                viewModel.selectAll()
            } else {
                viewModel.clearSelection()
            }
        },
        onClickSave = {
            val selectedIds = viewModel.getSelectedMediaIds()
            if (selectedIds.isEmpty()) return@GalleryScreen
            onClickNext(selectedIds)
        },
        onLongClickMediaItem = { mediaId ->
            // 전체 사진 모드에서는 전체 사진 목록 기준으로 미리보기를 표시한다.
            onLongClickMediaItem(null, mediaId)
        },
        onClickBack = onClickBack,
    )
}

/**
 * 갤러리 화면
 *
 * @param uiState 갤러리 화면 UI 상태
 * @param title 화면의 타이틀
 * @param onToggleMedia 미디어 선택 상태 토글 콜백
 * @param onClickSelectAll 전체 선택 버튼 클릭 이벤트 콜백
 * @param onClickSave 저장 버튼 클릭 이벤트 콜백
 * @param onLongClickMediaItem 미디어 아이템의 롱클릭 이벤트 콜백
 * @param onClickBack 뒤로가기 버튼 클릭 이벤트 콜백
 */
@Composable
private fun GalleryScreen(
    uiState: GalleryUiState,
    title: String,
    onToggleMedia: (MediaUiModel) -> Unit,
    onClickSelectAll: (Boolean) -> Unit,
    onClickSave: () -> Unit,
    onLongClickMediaItem: (Long) -> Unit,
    onClickBack: () -> Unit,
) {
    var isExitDialogVisible by remember { mutableStateOf(false) }
    var isFlingMode by rememberSaveable { mutableStateOf(false) }
    var flingIndex by rememberSaveable { mutableIntStateOf(0) }
    val horizontalPadding = 20.dp
    val gridState = rememberLazyGridState()

    val mediaList = uiState.cluster.mediaList
    val selectedMediaIds = when (uiState) {
        is GalleryUiState.SomeSelected -> uiState.selectedIds
        is GalleryUiState.Saving -> uiState.selectedIds
        else -> emptySet()
    }
    val totalCount = mediaList.size
    val selectedCount = selectedMediaIds.size
    val isAllSelected = totalCount > 0 && selectedCount == totalCount

    val selectMedia: (MediaUiModel) -> Unit = { media ->
        if (!selectedMediaIds.contains(media.id)) {
            onToggleMedia(media)
        }
    }
    val unselectMedia: (MediaUiModel) -> Unit = { media ->
        if (selectedMediaIds.contains(media.id)) {
            onToggleMedia(media)
        }
    }

    LaunchedEffect(mediaList.size) {
        val maxIndex = mediaList.size
        if (flingIndex > maxIndex) {
            flingIndex = maxIndex
        }
    }

    BackHandler(enabled = uiState is GalleryUiState.SomeSelected && !isExitDialogVisible) {
        isExitDialogVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ChacColors.Background)
                .padding(horizontal = horizontalPadding)
                .padding(bottom = 20.dp),
        ) {
            ChacTopBar(
                title = stringResource(R.string.gallery_top_bar_title),
                navigationContentDescription = stringResource(R.string.gallery_back_cd),
                onClickBack = {
                    if (uiState is GalleryUiState.SomeSelected) {
                        isExitDialogVisible = true
                    } else {
                        onClickBack()
                    }
                },
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = ChacTextStyles.SubTitle01,
                        color = ChacColors.Text01,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(
                            text = selectedCount.toString(),
                            style = ChacTextStyles.Number,
                            color = ChacColors.Text02,
                        )
                        Text(
                            text = stringResource(R.string.gallery_slash),
                            style = ChacTextStyles.Number,
                            color = ChacColors.Etc,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                        Text(
                            text = totalCount.toString(),
                            style = ChacTextStyles.Number,
                            color = ChacColors.Text02,
                        )
                    }
                }
                if (isAllSelected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(color = ChacColors.BackgroundPopup)
                            .border(
                                width = 1.dp,
                                color = ChacColors.Stroke01,
                                shape = RoundedCornerShape(16.dp),
                            )
                            .clickable { onClickSelectAll(false) }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.gallery_unselect_all),
                            style = ChacTextStyles.Caption,
                            color = ChacColors.Text02,
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(color = ChacColors.BackgroundPopup)
                            .clickable { onClickSelectAll(true) }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.gallery_select_all),
                            style = ChacTextStyles.Caption,
                            color = ChacColors.Text04Caption,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            GalleryModeToggle(
                isFlingMode = isFlingMode,
                onClickGrid = { isFlingMode = false },
                onClickFling = { isFlingMode = true },
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (isFlingMode) {
                GalleryFlingDeck(
                    mediaList = mediaList,
                    selectedMediaIds = selectedMediaIds,
                    currentIndex = flingIndex,
                    onSelectCurrent = { media ->
                        selectMedia(media)
                        flingIndex = (flingIndex + 1).coerceAtMost(mediaList.size)
                    },
                    onUnselectCurrent = { media ->
                        unselectMedia(media)
                        flingIndex = (flingIndex + 1).coerceAtMost(mediaList.size)
                    },
                    onRestart = { flingIndex = 0 },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScrollFadingEdge(
                            state = gridState,
                            top = 14.dp,
                            bottom = 14.dp,
                        ),
                    state = gridState,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                ) {
                    itemsIndexed(mediaList, key = { _, media -> media.id }) { _, media ->
                        GalleryPhotoItem(
                            media = media,
                            isSelected = selectedMediaIds.contains(media.id),
                            onToggle = { onToggleMedia(media) },
                            onLongClick = { onLongClickMediaItem(media.id) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onClickSave,
                enabled = uiState is GalleryUiState.SomeSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChacColors.Primary,
                    contentColor = ChacColors.TextBtn01,
                    disabledContainerColor = ChacColors.Disable,
                    disabledContentColor = ChacColors.TextBtn03,
                ),
            ) {
                val buttonText = when {
                    uiState is GalleryUiState.SomeSelected -> stringResource(R.string.common_next)
                    else -> stringResource(R.string.gallery_select_prompt)
                }
                Text(
                    text = buttonText,
                    style = ChacTextStyles.Btn,
                )
            }
        }

        if (uiState is GalleryUiState.Saving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ChacColors.Token00000040),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = ChacColors.Primary)
            }
        }
    }

    if (isExitDialogVisible) {
        GalleryExitDialog(
            onClickConfirm = {
                isExitDialogVisible = false
                onClickBack()
            },
            onDismiss = { isExitDialogVisible = false },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GalleryScreenPreview() {
    ChacTheme {
        GalleryScreen(
            uiState = GalleryUiState.NoneSelected(
                cluster = MediaClusterUiModel(
                    id = 1L,
                    address = "Jeju Trip",
                    formattedDate = "2024.01.15",
                    mediaList = List(40) { index ->
                        MediaUiModel(
                            id = index.toLong(),
                            uriString = "content://sample/$index",
                            dateTaken = 0L,
                            mediaType = MediaType.IMAGE,
                        )
                    },
                    thumbnailUriStrings = listOf(
                        "content://sample/0",
                        "content://sample/1",
                    ),
                ),
            ),
            title = "Jeju Trip",
            onToggleMedia = {},
            onClickSelectAll = {},
            onClickSave = {},
            onLongClickMediaItem = {},
            onClickBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GalleryScreenAllSelectedPreview() {
    ChacTheme {
        GalleryScreen(
            uiState = GalleryUiState.SomeSelected(
                cluster = MediaClusterUiModel(
                    id = 1L,
                    address = "Jeju Trip",
                    formattedDate = "2024.01.15",
                    mediaList = List(40) { index ->
                        MediaUiModel(
                            id = index.toLong(),
                            uriString = "content://sample/$index",
                            dateTaken = 0L,
                            mediaType = MediaType.IMAGE,
                        )
                    },
                    thumbnailUriStrings = listOf(
                        "content://sample/0",
                        "content://sample/1",
                    ),
                ),
                selectedIds = (0L until 40L).toSet(),
            ),
            title = stringResource(R.string.clustering_total_photo_title),
            onToggleMedia = {},
            onClickSelectAll = {},
            onClickSave = {},
            onLongClickMediaItem = {},
            onClickBack = {},
        )
    }
}
