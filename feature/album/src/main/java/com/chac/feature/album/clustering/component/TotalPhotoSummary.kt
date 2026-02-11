package com.chac.feature.album.clustering.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chac.core.designsystem.ui.theme.ChacColors
import com.chac.core.designsystem.ui.theme.ChacTextStyles
import com.chac.core.designsystem.ui.theme.ChacTheme
import com.chac.core.resources.R
import java.util.Locale

/**
 * 전체 사진 수를 표시한다
 *
 * @param totalCount 전체 사진 개수
 * @param onClick 클릭 이벤트 콜백
 */
@Composable
fun TotalPhotoSummary(
    totalCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .clickable(onClick = onClick)
            .background(
                color = ChacColors.BackgroundPopup,
                shape = shape,
            )
            .padding(all = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.clustering_total_photo_title),
            style = ChacTextStyles.ContentTitle,
            color = ChacColors.Text03,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = formatCount(totalCount),
                style = ChacTextStyles.Number,
                color = ChacColors.Text03,
            )

            TotalPhotoSummaryArrowIcon()
        }
    }
}

@Composable
private fun TotalPhotoSummaryArrowIcon(
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(width = 9.dp, height = 19.dp)) {
        val stroke = 1.5f * density
        drawLine(
            color = ChacColors.Text03,
            start = Offset(x = 3f / 9f * size.width, y = 6f / 19f * size.height),
            end = Offset(x = 6f / 9f * size.width, y = 9.5f / 19f * size.height),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = ChacColors.Text03,
            start = Offset(x = 6f / 9f * size.width, y = 9.5f / 19f * size.height),
            end = Offset(x = 3f / 9f * size.width, y = 13f / 19f * size.height),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

/**
 * 숫자 카운트를 로케일 기준 포맷 문자열로 변환한다
 *
 * @param count 포맷할 수량
 * @return 로케일 기준으로 포맷된 문자열
 */
internal fun formatCount(count: Int): String = String.format(Locale.getDefault(), "%,d", count)

@Preview(showBackground = true)
@Composable
private fun TotalPhotoSummaryPreview() {
    ChacTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            TotalPhotoSummary(
                totalCount = 99_990,
                onClick = {},
            )
        }
    }
}
