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
 * 선택되지 않은 체크박스 아이콘
 */
val ChacIcons.CheckUnselected: ImageVector
    get() = checkUnselectedIcon

private val checkUnselectedIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "CheckUnselectedIcon",
        defaultWidth = 20.dp,
        defaultHeight = 20.dp,
        viewportWidth = 20f,
        viewportHeight = 20f,
    ).apply {
        // 배경 원 (반투명 검정)
        path(
            fill = SolidColor(androidx.compose.ui.graphics.Color(0x66000000)),
        ) {
            moveTo(0f, 10f)
            arcToRelative(10f, 10f, 0f, true, true, 20f, 0f)
            arcToRelative(10f, 10f, 0f, true, true, -20f, 0f)
        }
        // 체크마크 (회색)
        path(
            stroke = SolidColor(androidx.compose.ui.graphics.Color(0xFFBBBBBB)),
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            moveTo(13.3333f, 7.5f)
            lineTo(8.74996f, 12.0833f)
            lineTo(6.66663f, 10f)
        }
    }.build()
}

@Preview
@Composable
private fun CheckUnselectedIconPreview() {
    Box(
        modifier = Modifier
            .background(ChacColors.Background)
            .padding(16.dp)
    ) {
        Icon(
            imageVector = ChacIcons.CheckUnselected,
            contentDescription = null,
            tint = androidx.compose.ui.graphics.Color.Unspecified,
        )
    }
}
