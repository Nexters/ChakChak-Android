package com.chac.feature.album.gallery

import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chac.core.designsystem.ui.component.ChacImage
import com.chac.core.designsystem.ui.icon.Alert
import com.chac.core.designsystem.ui.icon.Back
import com.chac.core.designsystem.ui.icon.ChacIcons
import com.chac.core.designsystem.ui.icon.CheckSelected
import com.chac.core.designsystem.ui.icon.CheckUnselected
import com.chac.core.designsystem.ui.theme.ChacColors
import com.chac.core.designsystem.ui.theme.ChacTextStyles
import com.chac.core.designsystem.ui.theme.ChacTheme
import com.chac.core.permission.compose.rememberWriteRequestLauncher
import com.chac.core.resources.R
import com.chac.domain.album.media.model.MediaType
import com.chac.feature.album.gallery.model.GalleryUiState
import com.chac.feature.album.model.MediaClusterUiModel
import com.chac.feature.album.model.MediaUiModel
import com.chac.feature.album.model.SaveUiStatus

/**
 * 갤러리 화면 라우트
 *
 * @param cluster 화면에 표시할 클러스터
 * @param viewModel 갤러리 화면의 뷰모델
 * @param onSaveCompleted 저장 완료 이후 동작을 전달하는 콜백
 * @param onClickBack 뒤로가기 버튼 클릭 이벤트 콜백
 */
@Composable
fun GalleryRoute(
    cluster: MediaClusterUiModel,
    viewModel: GalleryViewModel = hiltViewModel(),
    onSaveCompleted: (String, Int) -> Unit,
    onClickBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val writeRequestLauncher = rememberWriteRequestLauncher(
        onGranted = { viewModel.saveSelectedMedia() },
    )

    LaunchedEffect(viewModel) {
        viewModel.initialize(cluster)

        viewModel.saveCompletedEvents.collect { event ->
            onSaveCompleted(event.title, event.savedCount)
        }
    }

    GalleryScreen(
        uiState = uiState,
        onToggleMedia = viewModel::toggleSelection,
        onClickSelectAll = { selected: Boolean ->
            if (selected) {
                viewModel.selectAll()
            } else {
                viewModel.clearSelection()
            }
        },
        onClickSave = {
            val selectedMediaList = viewModel.getSelectedMediaList()

            if (selectedMediaList.isEmpty()) return@GalleryScreen

            val uris = selectedMediaList.map { it.uriString.toUri() }
            val intentSender = MediaStore.createWriteRequest(
                context.contentResolver,
                uris,
            ).intentSender

            writeRequestLauncher(intentSender)
        },
        onClickBack = onClickBack,
    )
}

/**
 * 갤러리 화면
 *
 * @param uiState 갤러리 화면 UI 상태
 * @param onToggleMedia 미디어 선택 상태 토글 콜백
 * @param onClickSelectAll 전체 선택 버튼 클릭 이벤트 콜백
 * @param onClickSave 저장 버튼 클릭 이벤트 콜백
 * @param onClickBack 뒤로가기 버튼 클릭 이벤트 콜백
 */
@Composable
private fun GalleryScreen(
    uiState: GalleryUiState,
    onToggleMedia: (MediaUiModel) -> Unit,
    onClickSelectAll: (Boolean) -> Unit,
    onClickSave: () -> Unit,
    onClickBack: () -> Unit,
) {
    var isExitDialogVisible by remember { mutableStateOf(false) }
    val cluster = uiState.cluster
    val title = cluster.title
    val mediaList = cluster.mediaList
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChacColors.Background)
            .padding(bottom = 20.dp),
    ) {
        GalleryTopBar(
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
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
                        .border(width = 1.dp, color = ChacColors.Stroke01, shape = RoundedCornerShape(16.dp))
                        .clickable {
                            onClickSelectAll(false)
                        }
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
                        .clickable {
                            onClickSelectAll(true)
                        }
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
        Spacer(modifier = Modifier.height(20.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
        ) {
            items(mediaList, key = { it.id }) { media ->
                GalleryPhotoItem(
                    media = media,
                    isSelected = selectedMediaIds.contains(media.id),
                    onToggle = { onToggleMedia(media) },
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onClickSave,
            enabled = uiState is GalleryUiState.SomeSelected,
            modifier = Modifier
                .padding(horizontal = 20.dp)
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
                uiState is GalleryUiState.SomeSelected -> stringResource(R.string.gallery_save_album_count, selectedCount)
                else -> stringResource(R.string.gallery_select_prompt)
            }
            Text(
                text = buttonText,
                style = ChacTextStyles.Btn,
            )
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
 * 갤러리 상단의 뒤로가기 버튼과 타이틀을 표시한다
 *
 * @param onClickBack 뒤로가기 버튼 클릭 이벤트 콜백
 */
@Composable
private fun GalleryTopBar(
    modifier: Modifier = Modifier,
    onClickBack: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(52.dp),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(
            onClick = onClickBack,
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            Icon(
                imageVector = ChacIcons.Back,
                contentDescription = stringResource(R.string.gallery_back_cd),
                tint = ChacColors.Text01,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = stringResource(R.string.gallery_top_bar_title),
            style = ChacTextStyles.Title,
            color = ChacColors.Text01,
        )
    }
}

/**
 * 사진 그리드의 선택 가능한 아이템을 표시한다
 *
 * @param media 이미지 모델
 * @param isSelected 선택 상태 여부
 * @param onToggle 선택 상태 토글 콜백
 */
@Composable
private fun GalleryPhotoItem(
    media: MediaUiModel,
    isSelected: Boolean,
    onToggle: () -> Unit,
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
            .clickable(onClick = onToggle),
    ) {
        ChacImage(
            model = media.uriString,
            modifier = Modifier.matchParentSize(),
        )
        Icon(
            imageVector = if (isSelected) ChacIcons.CheckSelected else ChacIcons.CheckUnselected,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(6.dp)
                .size(20.dp),
            tint = androidx.compose.ui.graphics.Color.Unspecified,
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
                    title = "Jeju Trip",
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
                    saveStatus = SaveUiStatus.Default,
                ),
            ),
            onToggleMedia = {},
            onClickSelectAll = {},
            onClickSave = {},
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
                    title = "Jeju Trip",
                    mediaList = List(40) { index ->
                        MediaUiModel(
                            id = index.toLong(),
                            uriString = "content://sample/$index",
                            dateTaken = 0L,
                            mediaType = MediaType.IMAGE,
                        )
                    },
                    saveStatus = SaveUiStatus.Default,
                ),
                selectedIds = (0L until 40L).toSet(),
            ),
            onToggleMedia = {},
            onClickSelectAll = {},
            onClickSave = {},
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
