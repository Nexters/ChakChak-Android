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
 * 저장 완료 배지 배경 아이콘
 */
val ChacIcons.SaveCompletedBadge: ImageVector
    get() = saveCompletedBadgeIcon

private val ChacIcons.saveCompletedBadgeIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "SaveCompletedBadgeIcon",
        defaultWidth = 16.dp,
        defaultHeight = 16.dp,
        viewportWidth = 16f,
        viewportHeight = 16f,
    ).apply {
        path(
            fill = SolidColor(Color(0xFF8A65FF)),
        ) {
            moveTo(8f, 0f)
            curveTo(12.4183f, 0f, 16f, 3.58172f, 16f, 8f)
            curveTo(16f, 12.4183f, 12.4183f, 16f, 8f, 16f)
            curveTo(3.58172f, 16f, 0f, 12.4183f, 0f, 8f)
            curveTo(0f, 3.58172f, 3.58172f, 0f, 8f, 0f)
            close()
        }

        path(
            fill = null,
            stroke = SolidColor(Color(0xFF141519)),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(10.6666f, 6f)
            lineTo(6.99992f, 9.66667f)
            lineTo(5.33325f, 8f)
        }
    }.build()
}

@Preview
@Composable
private fun SaveCompletedBadgeIconPreview() {
    Box(
        modifier = Modifier
            .background(ChacColors.Background)
            .padding(16.dp),
    ) {
        Icon(
            imageVector = ChacIcons.SaveCompletedBadge,
            contentDescription = null,
            tint = Color.Unspecified,
        )
    }
}
