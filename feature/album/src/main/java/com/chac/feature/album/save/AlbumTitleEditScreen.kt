package com.chac.feature.album.save

import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chac.core.designsystem.ui.component.ChacImage
import com.chac.core.designsystem.ui.icon.Back
import com.chac.core.designsystem.ui.icon.ChacIcons
import com.chac.core.designsystem.ui.icon.Remove
import com.chac.core.designsystem.ui.theme.ChacColors
import com.chac.core.designsystem.ui.theme.ChacTextStyles
import com.chac.core.designsystem.ui.theme.ChacTheme
import com.chac.core.permission.compose.rememberWriteRequestLauncher
import com.chac.core.resources.R
import com.chac.feature.album.gallery.model.SaveCompletedEvent

/**
 * 앨범명 수정 화면 라우트.
 *
 * @param clusterId 저장 대상 클러스터 ID (전체 사진 저장의 경우 null)
 * @param selectedMediaIds 저장 대상 미디어 ID 목록
 * @param viewModel 앨범명 수정 화면 ViewModel
 * @param onSaveCompleted 저장 완료 이후 동작 콜백
 * @param onClickBack 뒤로가기 버튼 클릭 이벤트 콜백
 */
@Composable
fun AlbumTitleEditRoute(
    clusterId: Long?,
    selectedMediaIds: List<Long>,
    viewModel: AlbumTitleEditViewModel = hiltViewModel(),
    onSaveCompleted: (String, Int) -> Unit,
    onClickBack: () -> Unit,
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    val writeRequestLauncher = rememberWriteRequestLauncher(
        onGranted = { viewModel.save() },
    )

    LaunchedEffect(viewModel, clusterId, selectedMediaIds) {
        viewModel.initialize(clusterId, selectedMediaIds)

        viewModel.saveCompletedEvents.collect { event: SaveCompletedEvent ->
            onSaveCompleted(event.title, event.savedCount)
        }
    }

    AlbumTitleEditScreen(
        title = uiState.title,
        placeholder = uiState.placeholder,
        selectedCount = uiState.selectedCount,
        thumbnailUriStrings = uiState.selectedUriStrings.take(2),
        isSaving = uiState.isSaving,
        onTitleChange = viewModel::updateTitle,
        onClearTitle = viewModel::clearTitle,
        onClickSave = {
            if (uiState.title.text.trim().isBlank()) return@AlbumTitleEditScreen

            val uris = uiState.selectedUriStrings.map { it.toUri() }
            if (uris.isEmpty()) return@AlbumTitleEditScreen

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
 * 앨범명 수정 화면 UI.
 *
 * ViewModel에 의존하지 않는 순수 UI Composable로, 외부에서 상태와 이벤트를 주입받아 렌더링한다.
 * 저장 중([isSaving])에는 전체 화면 오버레이 로딩을 표시하고 저장 버튼을 비활성화한다.
 *
 * @param title 현재 입력된 앨범명([TextFieldValue]는 selection/composition 상태를 포함)
 * @param placeholder 앨범명 입력창의 placeHolder
 * @param selectedCount 저장 대상 미디어 개수 (0이면 저장 버튼 비활성화)
 * @param thumbnailUriStrings 선택된 미디어의 대표 썸네일 URI 문자열 리스트
 * @param modifier 외부에서 주입하는 Modifier
 * @param isSaving 저장 진행 중 여부
 * @param onTitleChange 앨범명 변경 콜백
 * @param onClearTitle 앨범명 초기화(삭제) 콜백
 * @param onClickSave 저장 버튼 클릭 콜백 (권한/WriteRequest 등은 상위에서 처리)
 * @param onClickBack 뒤로가기 버튼 클릭 콜백
 */
@Composable
fun AlbumTitleEditScreen(
    title: TextFieldValue,
    placeholder: String,
    selectedCount: Int,
    thumbnailUriStrings: List<String>,
    modifier: Modifier = Modifier,
    isSaving: Boolean = false,
    onTitleChange: (TextFieldValue) -> Unit,
    onClearTitle: () -> Unit,
    onClickSave: () -> Unit,
    onClickBack: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ChacColors.Background)
            .windowInsetsPadding(WindowInsets.ime.exclude(WindowInsets.navigationBars))
            .padding(bottom = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AlbumTitleEditTopBar(
                onClickBack = onClickBack,
            )

            Spacer(modifier = Modifier.height(56.dp))

            ThumbnailImage(
                thumbnailUriStrings = thumbnailUriStrings,
            )

            Spacer(modifier = Modifier.height(28.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.album_title_edit_label),
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style = ChacTextStyles.SubTitle03,
                    color = ChacColors.Ffffff40,
                )

                TextField(
                    value = title,
                    onValueChange = onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    textStyle = ChacTextStyles.Body,
                    placeholder = {
                        Text(
                            text = placeholder,
                            style = ChacTextStyles.SubTitle01,
                            color = ChacColors.Ffffff40,
                        )
                    },
                    trailingIcon = {
                        if (title.text.isNotBlank()) {
                            IconButton(onClick = onClearTitle) {
                                Icon(
                                    imageVector = ChacIcons.Remove,
                                    contentDescription = null,
                                    tint = ChacColors.Text01,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(
                                            color = Color.White.copy(alpha = 0.1f),
                                            shape = CircleShape,
                                        )
                                        .padding(6.dp),
                                )
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = ChacColors.BackgroundPopup,
                        unfocusedContainerColor = ChacColors.BackgroundPopup,
                        disabledContainerColor = ChacColors.BackgroundPopup,
                        focusedIndicatorColor = ChacColors.BackgroundPopup,
                        unfocusedIndicatorColor = ChacColors.BackgroundPopup,
                        disabledIndicatorColor = ChacColors.BackgroundPopup,
                        cursorColor = ChacColors.Text01,
                        focusedTextColor = ChacColors.Text01,
                        unfocusedTextColor = ChacColors.Text01,
                    ),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onClickSave,
                enabled = !isSaving && selectedCount > 0 && title.text.trim().isNotBlank(),
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
                Text(
                    text = stringResource(R.string.gallery_save_album_count, selectedCount),
                    style = ChacTextStyles.Btn,
                )
            }
        }

        if (isSaving) {
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
}

/**
 * 앨범명 수정 화면 상단 바.
 *
 * @param onClickBack 뒤로가기 버튼 클릭 콜백
 */
@Composable
private fun AlbumTitleEditTopBar(
    modifier: Modifier = Modifier,
    onClickBack: () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        IconButton(
            onClick = onClickBack,
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            Icon(
                imageVector = ChacIcons.Back,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .offset(x = (-12).dp), // IconButton로 인한 패딩만큼 왼쪽으로 이동
                tint = ChacColors.Text01,
            )
        }
        Text(
            text = stringResource(R.string.album_title_edit_title),
            style = ChacTextStyles.Title,
            color = ChacColors.Text01,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

/**
 * 썸네일 이미지
 *
 * @param thumbnailUriStrings 선택된 미디어의 대표 썸네일 URI 문자열 리스트
 */
@Composable
private fun ThumbnailImage(
    thumbnailUriStrings: List<String>,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)

    Box(modifier = modifier) {
        thumbnailUriStrings.forEachIndexed { index, uri ->
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .rotate(10f * index)
                    .clip(shape)
                    .background(ChacColors.BackgroundPopup)
                    .zIndex(thumbnailUriStrings.lastIndex - index.toFloat()), // 이미지 중첩 렌더링 순서 보정
            ) {
                ChacImage(
                    model = uri,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                )

                // dim
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.6f), shape),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AlbumTitleEditScreenPreview() {
    ChacTheme {
        AlbumTitleEditScreen(
            title = TextFieldValue(""),
            placeholder = "부산 광역시",
            selectedCount = 4,
            thumbnailUriStrings = listOf(
                "content://sample/0",
                "content://sample/1",
            ),
            onTitleChange = {},
            onClearTitle = {},
            onClickSave = {},
        ) {}
    }
}
