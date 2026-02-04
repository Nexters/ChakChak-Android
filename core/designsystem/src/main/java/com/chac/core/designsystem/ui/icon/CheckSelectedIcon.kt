package com.chac.core.designsystem.ui.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chac.core.designsystem.ui.theme.ChacColors

/**
 * 선택된 체크박스 아이콘
 */
val ChacIcons.CheckSelected: ImageVector
    get() = checkSelectedIcon

private val checkSelectedIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "CheckSelectedIcon",
        defaultWidth = 20.dp,
        defaultHeight = 20.dp,
        viewportWidth = 20f,
        viewportHeight = 20f,
    ).apply {
        // 배경 사각형 (보라색)
        path(
            fill = SolidColor(ChacColors.Primary),
        ) {
            moveTo(9.5f, 0.5f)
            horizontalLineTo(10.5f)
            arcTo(9.5f, 9.5f, 0f, false, true, 19.5f, 9.5f)
            verticalLineTo(10.5f)
            arcTo(9.5f, 9.5f, 0f, false, true, 10.5f, 19.5f)
            horizontalLineTo(9.5f)
            arcTo(9.5f, 9.5f, 0f, false, true, 0.5f, 10.5f)
            verticalLineTo(9.5f)
            arcTo(9.5f, 9.5f, 0f, false, true, 9.5f, 0.5f)
            close()
        }
        // 테두리 (연보라색)
        path(
            stroke = SolidColor(androidx.compose.ui.graphics.Color(0xFFB299FF)),
            strokeLineWidth = 1f,
        ) {
            moveTo(9.5f, 0.5f)
            horizontalLineTo(10.5f)
            arcTo(9.5f, 9.5f, 0f, false, true, 19.5f, 9.5f)
            verticalLineTo(10.5f)
            arcTo(9.5f, 9.5f, 0f, false, true, 10.5f, 19.5f)
            horizontalLineTo(9.5f)
            arcTo(9.5f, 9.5f, 0f, false, true, 0.5f, 10.5f)
            verticalLineTo(9.5f)
            arcTo(9.5f, 9.5f, 0f, false, true, 9.5f, 0.5f)
            close()
        }
        // 체크마크 (흰색)
        path(
            stroke = SolidColor(androidx.compose.ui.graphics.Color.White),
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(13.3334f, 7.5f)
            lineTo(8.75002f, 12.0833f)
            lineTo(6.66669f, 10f)
        }
    }.build()
}

@Preview
@Composable
private fun CheckSelectedIconPreview() {
    Box(
        modifier = Modifier
            .background(ChacColors.Background)
            .padding(16.dp)
    ) {
        Icon(
            imageVector = ChacIcons.CheckSelected,
            contentDescription = null,
            tint = androidx.compose.ui.graphics.Color.Unspecified,
        )
    }
}
