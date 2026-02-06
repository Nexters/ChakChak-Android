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
 * 닫기 아이콘
 */
val ChacIcons.Close: ImageVector
    get() = closeIcon

private val ChacIcons.closeIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "CloseIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color(0xFFF6F6F6)),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(18f, 6f)
            lineTo(6f, 18f)

            moveTo(6f, 6f)
            lineTo(18f, 18f)
        }
    }.build()
}

@Preview
@Composable
private fun CloseIconPreview() {
    Box(
        modifier = Modifier
            .background(ChacColors.Background)
            .padding(16.dp),
    ) {
        Icon(
            imageVector = ChacIcons.Close,
            contentDescription = null,
            tint = Color.Gray,
        )
    }
}
