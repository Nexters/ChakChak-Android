package com.chac.feature.album.gallery

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chac.core.designsystem.ui.component.ChacImage
import com.chac.core.designsystem.ui.component.ChacTopBar
import com.chac.core.designsystem.ui.icon.Alert
import com.chac.core.designsystem.ui.icon.ChacIcons
import com.chac.core.designsystem.ui.icon.CheckSelected
import com.chac.core.designsystem.ui.icon.CheckUnselected
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

    BackHandler(enabled = uiState is GalleryUiState.SomeSelected && !isExitDialogVisible) {
        isExitDialogVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ChacColors.Background)
                .padding(horizontal = 20.dp)
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
                itemsIndexed(mediaList, key = { _, media -> media.id }) { index, media ->
                    GalleryPhotoItem(
                        media = media,
                        isSelected = selectedMediaIds.contains(media.id),
                        onToggle = { onToggleMedia(media) },
                        onLongClick = { onLongClickMediaItem(media.id) },
                    )
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

/**
 * 선택 상태에서 페이지 이탈을 확인하는 대화상자를 표시한다
 *
 * @param onClickConfirm 확인 버튼 클릭 이벤트 콜백
 * @param onDismiss 대화상자 닫힘 이벤트 콜백 (ex. 취소 또는 바깥 영역 클릭)
 */
@Composable
private fun GalleryExitDialog(
    modifier: Modifier = Modifier,
    onClickConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ChacColors.BackgroundPopup,
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    imageVector = ChacIcons.Alert,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.gallery_exit_title),
                    style = ChacTextStyles.Headline02,
                    color = ChacColors.Text01,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.gallery_exit_message),
                    style = ChacTextStyles.Body,
                    color = ChacColors.Text03,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(30.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChacColors.Sub04,
                            contentColor = ChacColors.TextBtn02,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.gallery_exit_cancel),
                            style = ChacTextStyles.Btn,
                            color = ChacColors.Primary,
                        )
                    }
                    Button(
                        onClick = onClickConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChacColors.Primary,
                            contentColor = ChacColors.TextBtn01,
                        ),
                    ) {
                        Text(
                            text = stringResource(R.string.gallery_exit_confirm),
                            style = ChacTextStyles.Btn,
                            color = ChacColors.TextBtn01,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 사진 그리드의 선택 가능한 아이템을 표시한다
 *
 * @param media 이미지 모델
 * @param isSelected 선택 상태 여부
 * @param onToggle 선택 상태 토글 콜백
 * @param onLongClick 롱클릭 이벤트 콜백
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GalleryPhotoItem(
    media: MediaUiModel,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isSelected) {
                    Modifier
                        .border(
                            width = 1.dp,
                            color = ChacColors.Primary,
                            shape = RoundedCornerShape(12.dp),
                        )
                } else {
                    Modifier
                },
            )
            .background(ChacColors.BackgroundPopup)
            .combinedClickable(
                onClick = onToggle,
                onLongClick = onLongClick,
            ),
    ) {
        ChacImage(
            model = media.uriString,
            modifier = Modifier.matchParentSize(),
        )

        // dim
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(alpha = 0.6f)
                    .background(color = Color.Black),
            )
        }

        Icon(
            imageVector = if (isSelected) ChacIcons.CheckSelected else ChacIcons.CheckUnselected,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(6.dp)
                .size(20.dp),
            tint = Color.Unspecified,
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

@Preview(showBackground = true)
@Composable
private fun GalleryExitDialogPreview() {
    ChacTheme {
        GalleryExitDialog(
            onClickConfirm = {},
            onDismiss = {},
        )
    }
}
