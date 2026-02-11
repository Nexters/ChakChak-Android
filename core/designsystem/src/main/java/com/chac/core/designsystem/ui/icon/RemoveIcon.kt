package com.chac.core.designsystem.ui.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chac.core.designsystem.ui.theme.ChacColors

/**
 * X 아이콘 (8x8).
 *
 * - viewBox: 0 0 8 8
 * - stroke: #F6F6F6
 * - stroke-width: 1.8
 * - stroke-linecap: round
 */
val ChacIcons.Remove: ImageVector
    get() = removeIcon

private val ChacIcons.removeIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "removeIcon",
        defaultWidth = 8.dp,
        defaultHeight = 8.dp,
        viewportWidth = 8f,
        viewportHeight = 8f,
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color(0xFFF6F6F6)),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(0.9f, 6.9f)
            lineTo(6.9f, 0.9f)
        }

        path(
            fill = null,
            stroke = SolidColor(Color(0xFFF6F6F6)),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(6.9f, 6.9f)
            lineTo(0.9f, 0.9f)
        }
    }.build()
}

@Preview
@Composable
private fun Vector17IconPreview() {
    Box(
        modifier = Modifier
            .background(ChacColors.Background)
            .padding(16.dp),
    ) {
        Icon(
            imageVector = ChacIcons.Remove,
            contentDescription = null,
            tint = Color.Unspecified,
        )
    }
}
