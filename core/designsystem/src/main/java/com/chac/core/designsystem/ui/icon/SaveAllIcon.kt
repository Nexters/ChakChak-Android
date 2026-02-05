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
 *  전체 저장 아이콘
 */
val ChacIcons.SaveAll: ImageVector
    get() = saveAllIcon

private val ChacIcons.saveAllIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "SaveAllIcon",
        defaultWidth = 14.dp,
        defaultHeight = 14.dp,
        viewportWidth = 14f,
        viewportHeight = 14f,
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(12.75f, 8.75f)
            verticalLineTo(11.4167f)
            curveTo(12.75f, 11.7703f, 12.6095f, 12.1094f, 12.3595f, 12.3595f)
            curveTo(12.1094f, 12.6095f, 11.7703f, 12.75f, 11.4167f, 12.75f)
            horizontalLineTo(2.08333f)
            curveTo(1.72971f, 12.75f, 1.39057f, 12.6095f, 1.14052f, 12.3595f)
            curveTo(0.890476f, 12.1094f, 0.75f, 11.7703f, 0.75f, 11.4167f)
            verticalLineTo(8.75f)

            moveTo(3.41667f, 5.41667f)
            lineTo(6.75f, 8.75f)

            moveTo(6.75f, 8.75f)
            lineTo(10.0833f, 5.41667f)

            moveTo(6.75f, 8.75f)
            verticalLineTo(0.75f)
        }
    }.build()
}

@Preview
@Composable
private fun SaveAllIconPreview() {
    Box(
        modifier = Modifier
            .background(ChacColors.Background)
            .padding(16.dp),
    ) {
        Icon(
            imageVector = ChacIcons.SaveAll,
            contentDescription = null,
            tint = Color.Gray,
        )
    }
}
