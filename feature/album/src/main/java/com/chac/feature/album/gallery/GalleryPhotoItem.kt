package com.chac.feature.album.gallery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chac.core.designsystem.ui.component.ChacImage
import com.chac.core.designsystem.ui.icon.ChacIcons
import com.chac.core.designsystem.ui.icon.CheckSelected
import com.chac.core.designsystem.ui.icon.CheckUnselected
import com.chac.core.designsystem.ui.theme.ChacColors
import com.chac.core.designsystem.ui.theme.ChacTheme
import com.chac.domain.album.media.model.MediaType
import com.chac.feature.album.model.MediaUiModel

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
internal fun GalleryPhotoItem(
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

/**
 * 선택되지 않은 상태의 그리드 아이템 미리보기.
 */
@Preview(showBackground = true)
@Composable
private fun GalleryPhotoItemPreview() {
    ChacTheme {
        Box(modifier = Modifier.size(120.dp)) {
            GalleryPhotoItem(
                media = previewMedia,
                isSelected = false,
                onToggle = {},
                onLongClick = {},
            )
        }
    }
}

/**
 * 선택된 상태의 그리드 아이템 미리보기.
 */
@Preview(showBackground = true)
@Composable
private fun GalleryPhotoItemSelectedPreview() {
    ChacTheme {
        Box(modifier = Modifier.size(120.dp)) {
            GalleryPhotoItem(
                media = previewMedia,
                isSelected = true,
                onToggle = {},
                onLongClick = {},
            )
        }
    }
}

private val previewMedia = MediaUiModel(
    id = 1L,
    uriString = "content://sample/1",
    dateTaken = 0L,
    mediaType = MediaType.IMAGE,
)
