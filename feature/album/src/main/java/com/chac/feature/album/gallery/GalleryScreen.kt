package com.chac.feature.album.gallery

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chac.core.designsystem.ui.theme.ChacTheme
import com.chac.core.resources.R
import androidx.compose.ui.window.Dialog
import com.chac.domain.album.media.MediaType
import com.chac.feature.album.gallery.model.GalleryUiState
import com.chac.feature.album.model.MediaUiModel

/**
 * 갤러리 화면 라우트
 *
 * @param title 화면에 표시할 앨범 제목
 * @param mediaList 화면에 표시할 미디어 목록
 * @param onBack 뒤로가기 동작을 전달하는 콜백
 * @param viewModel 갤러리 화면 ViewModel
 */
@Composable
fun GalleryRoute(
    title: String,
    mediaList: List<MediaUiModel>,
    onBack: () -> Unit,
    viewModel: GalleryViewModel = viewModel(factory = GalleryViewModel.provideFactory(title, mediaList)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    GalleryScreen(
        uiState = uiState,
        onToggleMedia = viewModel::toggleSelection,
        onSelectAll = viewModel::selectAll,
        onClearSelection = viewModel::clearSelection,
        onBack = onBack,
    )
}

/**
 * 갤러리 화면
 *
 * @param uiState 갤러리 화면 UI 상태
 * @param onToggleMedia 미디어 선택 상태 토글 콜백
 * @param onSelectAll 전체 선택 콜백
 * @param onClearSelection 전체 선택 해제 콜백
 * @param onBack 뒤로가기 동작을 전달하는 콜백
 */
@Composable
private fun GalleryScreen(
    uiState: GalleryUiState,
    onToggleMedia: (MediaUiModel) -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onBack: () -> Unit,
) {
    var isExitDialogVisible by remember { mutableStateOf(false) }
    val (title, mediaList, selectedMediaIds) = when (uiState) {
        is GalleryUiState.NoneSelected -> Triple(uiState.title, uiState.mediaList, emptySet())
        is GalleryUiState.SomeSelected -> Triple(uiState.title, uiState.mediaList, uiState.selectedIds)
    }
    val totalCount = mediaList.size
    val selectedCount = selectedMediaIds.size
    val isAllSelected = totalCount > 0 && selectedCount == totalCount

    BackHandler(enabled = selectedCount > 0 && !isExitDialogVisible) {
        isExitDialogVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 20.dp),
    ) {
        GalleryTopBar(
            onBack = {
                if (selectedCount > 0) {
                    isExitDialogVisible = true
                } else {
                    onBack()
                }
            },
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.gallery_selected_count,
                        selectedCount,
                        totalCount,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(
                onClick = {
                    if (isAllSelected) {
                        onClearSelection()
                    } else {
                        onSelectAll()
                    }
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.gallery_select_all),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp),
        ) {
            items(mediaList, key = { it.id }) { media ->
                GalleryPhotoItem(
                    isSelected = selectedMediaIds.contains(media.id),
                    onToggle = { onToggleMedia(media) },
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = selectedCount > 0,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            val buttonText = if (selectedCount > 0) {
                stringResource(R.string.gallery_save_album_count, selectedCount)
            } else {
                stringResource(R.string.gallery_select_prompt)
            }
            Text(text = buttonText)
        }
    }

    if (isExitDialogVisible) {
        GalleryExitDialog(
            onConfirm = {
                isExitDialogVisible = false
                onBack()
            },
            onDismiss = { isExitDialogVisible = false },
        )
    }
}

/**
 * 선택 상태에서 페이지 이탈을 확인하는 대화상자를 표시한다
 *
 * @param onConfirm 확인 버튼 클릭 콜백
 * @param onDismiss 취소 또는 바깥 영역 클릭 콜백
 */
@Composable
private fun GalleryExitDialog(
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "!",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.gallery_exit_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.gallery_exit_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Text(text = stringResource(R.string.gallery_exit_cancel))
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSurface,
                            contentColor = MaterialTheme.colorScheme.surface,
                        ),
                    ) {
                        Text(text = stringResource(R.string.gallery_exit_confirm))
                    }
                }
            }
        }
    }
}

/**
 * 갤러리 상단의 뒤로가기 버튼과 타이틀을 표시한다
 *
 * @param onBack 뒤로가기 콜백
 */
@Composable
private fun GalleryTopBar(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.gallery_back_cd),
            )
        }
        Text(
            text = stringResource(R.string.gallery_top_bar_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * 사진 그리드의 선택 가능한 아이템을 표시한다
 *
 * @param isSelected 선택 상태 여부
 * @param onToggle 선택 상태 토글 콜백
 */
@Composable
private fun GalleryPhotoItem(
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onToggle),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(20.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = CircleShape,
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GalleryScreenPreview() {
    ChacTheme {
        GalleryScreen(
            uiState = GalleryUiState.NoneSelected(
                title = "Jeju Trip",
                mediaList = List(40) { index ->
                    MediaUiModel(
                        id = index.toLong(),
                        uriString = "content://sample/$index",
                        dateTaken = 0L,
                        mediaType = MediaType.IMAGE,
                    )
                },
            ),
            onToggleMedia = {},
            onSelectAll = {},
            onClearSelection = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GalleryExitDialogPreview() {
    ChacTheme {
        GalleryExitDialog(
            onConfirm = {},
            onDismiss = {},
        )
    }
}
