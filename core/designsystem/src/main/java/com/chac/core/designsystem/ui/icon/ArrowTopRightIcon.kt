package com.chac.core.designsystem.ui.icon

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * 우상단 대각선 화살표 아이콘
 */
val ChacIcons.ArrowTopRight: ImageVector
    get() = arrowTopRightIcon

private val arrowTopRightIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "ArrowTopRightIcon",
        defaultWidth = 14.dp,
        defaultHeight = 14.dp,
        viewportWidth = 14f,
        viewportHeight = 14f,
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
        ) {
            moveTo(3.11765f, 1f)
            horizontalLineTo(13f)
            moveTo(13f, 1f)
            verticalLineTo(10.8824f)
            moveTo(13f, 1f)
            lineTo(1f, 13f)
        }
    }.build()
}

@Preview
@Composable
private fun ArrowTopRightIconPreview() {
    Box(
        modifier = Modifier,
    ) {
        Icon(
            imageVector = ChacIcons.ArrowTopRight,
            contentDescription = null,
            tint = Color.White,
        )
    }
}
