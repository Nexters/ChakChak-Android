package com.chac.feature.album.gallery

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chac.core.designsystem.ui.component.ChacImage
import com.chac.core.designsystem.ui.icon.ChacIcons
import com.chac.core.designsystem.ui.icon.CheckSelected
import com.chac.core.designsystem.ui.icon.CheckUnselected
import com.chac.core.designsystem.ui.theme.ChacColors
import com.chac.core.designsystem.ui.theme.ChacTextStyles
import com.chac.core.designsystem.ui.theme.ChacTheme
import com.chac.core.resources.R
import com.chac.domain.album.media.model.MediaType
import com.chac.feature.album.model.MediaUiModel
import kotlinx.coroutines.launch

/**
 * 갤러리 모드(그리드/플링) 전환 칩을 표시한다.
 *
 * @param isFlingMode 현재 플링 모드 여부
 * @param onClickGrid 그리드 모드 선택 콜백
 * @param onClickFling 플링 모드 선택 콜백
 */
@Composable
internal fun GalleryModeToggle(
    isFlingMode: Boolean,
    onClickGrid: () -> Unit,
    onClickFling: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GalleryModeChip(
            text = stringResource(R.string.gallery_mode_grid),
            isSelected = !isFlingMode,
            onClick = onClickGrid,
            modifier = Modifier.weight(1f),
        )
        GalleryModeChip(
            text = stringResource(R.string.gallery_mode_fling),
            isSelected = isFlingMode,
            onClick = onClickFling,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * 단일 모드 칩을 표시한다.
 *
 * @param text 칩에 표시할 텍스트
 * @param isSelected 선택 상태 여부
 * @param onClick 클릭 콜백
 */
@Composable
private fun GalleryModeChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) {
                    ChacColors.Primary
                } else {
                    ChacColors.BackgroundPopup
                },
            )
            .border(
                width = 1.dp,
                color = if (isSelected) ChacColors.Primary else ChacColors.Stroke01,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = ChacTextStyles.Caption,
            color = if (isSelected) ChacColors.TextBtn01 else ChacColors.Text02,
        )
    }
}

/**
 * 플링 카드 덱 영역을 표시한다.
 *
 * @param mediaList 표시할 미디어 목록
 * @param selectedMediaIds 선택된 미디어 ID 집합
 * @param currentIndex 현재 표시 중인 인덱스
 * @param onSelectCurrent 현재 카드 선택 콜백
 * @param onUnselectCurrent 현재 카드 선택 해제 콜백
 * @param onRestart 카드 탐색을 처음부터 다시 시작하는 콜백
 */
@Composable
internal fun GalleryFlingDeck(
    mediaList: List<MediaUiModel>,
    selectedMediaIds: Set<Long>,
    currentIndex: Int,
    onSelectCurrent: (MediaUiModel) -> Unit,
    onUnselectCurrent: (MediaUiModel) -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentMedia = mediaList.getOrNull(currentIndex)
    val currentProgress = when {
        mediaList.isEmpty() -> 0
        currentIndex >= mediaList.size -> mediaList.size
        else -> currentIndex + 1
    }

    Box(modifier = modifier) {
        when {
            mediaList.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.gallery_select_prompt),
                        style = ChacTextStyles.Body,
                        color = ChacColors.Text03,
                    )
                }
            }

            currentMedia == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.gallery_fling_done),
                            style = ChacTextStyles.SubTitle01,
                            color = ChacColors.Text01,
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(ChacColors.BackgroundPopup)
                                .border(
                                    width = 1.dp,
                                    color = ChacColors.Stroke01,
                                    shape = RoundedCornerShape(12.dp),
                                )
                                .clickable(onClick = onRestart)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.gallery_fling_restart),
                                style = ChacTextStyles.Caption,
                                color = ChacColors.Text02,
                            )
                        }
                    }
                }
            }

            else -> {
                GalleryFlingCard(
                    media = currentMedia,
                    isSelected = selectedMediaIds.contains(currentMedia.id),
                    onSwipeRight = { onSelectCurrent(currentMedia) },
                    onSwipeLeft = { onUnselectCurrent(currentMedia) },
                    modifier = Modifier.fillMaxSize(),
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.36f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.gallery_fling_hint),
                        style = ChacTextStyles.Caption,
                        color = ChacColors.TextBtn01,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.gallery_fling_progress, currentProgress, mediaList.size),
                        style = ChacTextStyles.Caption,
                        color = ChacColors.TextBtn01,
                        textAlign = TextAlign.Center,
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GalleryFlingDecisionButton(
                        text = stringResource(R.string.gallery_fling_unselect),
                        isPrimary = false,
                        onClick = { onUnselectCurrent(currentMedia) },
                        modifier = Modifier.weight(1f),
                    )
                    GalleryFlingDecisionButton(
                        text = stringResource(R.string.gallery_fling_select),
                        isPrimary = true,
                        onClick = { onSelectCurrent(currentMedia) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/**
 * 플링 결과를 명시적으로 선택하는 하단 버튼을 표시한다.
 *
 * @param text 버튼 텍스트
 * @param isPrimary 강조 버튼 여부
 * @param onClick 클릭 콜백
 */
@Composable
private fun GalleryFlingDecisionButton(
    text: String,
    isPrimary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isPrimary) ChacColors.Primary else ChacColors.BackgroundPopup)
            .border(
                width = 1.dp,
                color = if (isPrimary) ChacColors.Primary else ChacColors.Stroke01,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = ChacTextStyles.Caption,
            color = if (isPrimary) ChacColors.TextBtn01 else ChacColors.Text02,
        )
    }
}

/**
 * 좌우 플링 제스처로 선택/해제를 처리하는 카드 본문을 표시한다.
 *
 * @param media 현재 표시할 미디어
 * @param isSelected 현재 미디어 선택 상태
 * @param onSwipeRight 우측 플링 완료 콜백
 * @param onSwipeLeft 좌측 플링 완료 콜백
 */
@Composable
private fun GalleryFlingCard(
    media: MediaUiModel,
    isSelected: Boolean,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var offsetX by remember(media.id) { mutableFloatStateOf(0f) }
    var offsetY by remember(media.id) { mutableFloatStateOf(0f) }

    BoxWithConstraints(modifier = modifier) {
        val dismissThresholdPx = with(density) { 96.dp.toPx() }
        val dismissOffsetPx = with(density) { maxWidth.toPx() + 160.dp.toPx() }
        val selectProgress = (offsetX / dismissThresholdPx).coerceIn(0f, 1f)
        val unselectProgress = (-offsetX / dismissThresholdPx).coerceIn(0f, 1f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChacColors.Background)
                .graphicsLayer {
                    translationX = offsetX
                    translationY = offsetY
                    rotationZ = (offsetX / 30f).coerceIn(-14f, 14f)
                }
                .pointerInput(media.id, dismissThresholdPx, dismissOffsetPx) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        },
                        onDragCancel = {
                            scope.launch {
                                launch {
                                    animate(
                                        initialValue = offsetX,
                                        targetValue = 0f,
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    ) { value, _ ->
                                        offsetX = value
                                    }
                                }
                                launch {
                                    animate(
                                        initialValue = offsetY,
                                        targetValue = 0f,
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    ) { value, _ ->
                                        offsetY = value
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            val shouldSelect = offsetX > dismissThresholdPx
                            val shouldUnselect = offsetX < -dismissThresholdPx
                            when {
                                shouldSelect -> {
                                    scope.launch {
                                        animate(
                                            initialValue = offsetX,
                                            targetValue = dismissOffsetPx,
                                            animationSpec = tween(durationMillis = 180),
                                        ) { value, _ ->
                                            offsetX = value
                                        }
                                        onSwipeRight()
                                    }
                                }

                                shouldUnselect -> {
                                    scope.launch {
                                        animate(
                                            initialValue = offsetX,
                                            targetValue = -dismissOffsetPx,
                                            animationSpec = tween(durationMillis = 180),
                                        ) { value, _ ->
                                            offsetX = value
                                        }
                                        onSwipeLeft()
                                    }
                                }

                                else -> {
                                    scope.launch {
                                        launch {
                                            animate(
                                                initialValue = offsetX,
                                                targetValue = 0f,
                                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                            ) { value, _ ->
                                                offsetX = value
                                            }
                                        }
                                        launch {
                                            animate(
                                                initialValue = offsetY,
                                                targetValue = 0f,
                                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                            ) { value, _ ->
                                                offsetY = value
                                            }
                                        }
                                    }
                                }
                            }
                        },
                    )
                },
        ) {
            ChacImage(
                model = media.uriString,
                modifier = Modifier.matchParentSize(),
            )
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .alpha(0.45f)
                        .background(Color.Black),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                GalleryFlingBadge(
                    text = stringResource(R.string.gallery_fling_unselect),
                    progress = unselectProgress,
                    isSelect = false,
                )
                GalleryFlingBadge(
                    text = stringResource(R.string.gallery_fling_select),
                    progress = selectProgress,
                    isSelect = true,
                )
            }

            Icon(
                imageVector = if (isSelected) ChacIcons.CheckSelected else ChacIcons.CheckUnselected,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .size(24.dp),
                tint = Color.Unspecified,
            )
        }
    }
}

/**
 * 플링 방향 힌트 배지를 표시한다.
 *
 * @param text 배지 텍스트
 * @param progress 플링 진행도(0f~1f)
 * @param isSelect 선택 방향 배지 여부
 */
@Composable
private fun GalleryFlingBadge(
    text: String,
    progress: Float,
    isSelect: Boolean,
    modifier: Modifier = Modifier,
) {
    val isActive = progress > 0f
    val activeBackground = if (isSelect) ChacColors.Primary else Color.Black
    val backgroundAlpha = if (isActive) (0.45f + (progress * 0.45f)).coerceAtMost(0.9f) else 0.62f

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                color = if (isActive) {
                    activeBackground.copy(alpha = backgroundAlpha)
                } else {
                    Color.Black.copy(alpha = backgroundAlpha)
                },
            )
            .border(
                width = 1.dp,
                color = if (isActive) ChacColors.TextBtn01 else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            style = ChacTextStyles.Caption,
            color = ChacColors.TextBtn01,
        )
    }
}

/**
 * 갤러리 모드 전환 영역 미리보기.
 */
@Preview(showBackground = true)
@Composable
private fun GalleryModeTogglePreview() {
    ChacTheme {
        Box(
            modifier = Modifier
                .background(ChacColors.Background)
                .padding(16.dp),
        ) {
            GalleryModeToggle(
                isFlingMode = false,
                onClickGrid = {},
                onClickFling = {},
            )
        }
    }
}

/**
 * 플링 덱 영역 미리보기.
 */
@Preview(showBackground = true)
@Composable
private fun GalleryFlingDeckPreview() {
    ChacTheme {
        Box(
            modifier = Modifier
                .background(ChacColors.Background)
                .padding(16.dp),
        ) {
            GalleryFlingDeck(
                mediaList = previewFlingMediaList,
                selectedMediaIds = setOf(1L),
                currentIndex = 0,
                onSelectCurrent = {},
                onUnselectCurrent = {},
                onRestart = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
            )
        }
    }
}

private val previewFlingMediaList = listOf(
    MediaUiModel(
        id = 1L,
        uriString = "content://sample/fling/1",
        dateTaken = 0L,
        mediaType = MediaType.IMAGE,
    ),
    MediaUiModel(
        id = 2L,
        uriString = "content://sample/fling/2",
        dateTaken = 0L,
        mediaType = MediaType.IMAGE,
    ),
)
