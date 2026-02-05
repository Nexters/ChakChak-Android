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
 * 오른쪽 화살표 아이콘
 */
val ChacIcons.ArrowRight: ImageVector
    get() = arrowRightIcon

private val ChacIcons.arrowRightIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "ArrowRightIcon",
        defaultWidth = 7.dp,
        defaultHeight = 11.dp,
        viewportWidth = 7f,
        viewportHeight = 11f,
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(0.9f, 0.9f)
            lineTo(5.4f, 5.4f)
            lineTo(0.9f, 9.9f)
        }
    }.build()
}

@Preview
@Composable
private fun ArrowRightIconPreview() {
    Box(
        modifier = Modifier
            .background(ChacColors.Background)
            .padding(16.dp),
    ) {
        Icon(
            imageVector = ChacIcons.ArrowRight,
            contentDescription = null,
            tint = Color.Gray,
        )
    }
}
