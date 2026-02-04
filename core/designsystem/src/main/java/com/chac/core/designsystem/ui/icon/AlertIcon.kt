package com.chac.core.designsystem.ui.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chac.core.designsystem.ui.theme.ChacColors

/**
 * 경고/알림 아이콘 (느낌표)
 */
val ChacIcons.Alert: ImageVector
    get() = alertIcon

private val alertIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "AlertIcon",
        defaultWidth = 50.dp,
        defaultHeight = 50.dp,
        viewportWidth = 50f,
        viewportHeight = 50f,
    ).apply {
        // 배경 원
        path(
            fill = SolidColor(ChacColors.Background),
        ) {
            moveTo(0f, 25f)
            arcToRelative(25f, 25f, 0f, true, true, 50f, 0f)
            arcToRelative(25f, 25f, 0f, true, true, -50f, 0f)
        }
        // 느낌표
        path(
            fill = SolidColor(ChacColors.Text01),
        ) {
            moveTo(26.3184f, 17.8594f)
            lineTo(26.1035f, 27.8398f)
            horizontalLineTo(23.8965f)
            lineTo(23.6621f, 17.8594f)
            horizontalLineTo(26.3184f)
            close()
            moveTo(23.4863f, 30.6328f)
            curveTo(23.4766f, 29.8125f, 24.1699f, 29.1387f, 25.0098f, 29.1289f)
            curveTo(25.8203f, 29.1387f, 26.5137f, 29.8125f, 26.5137f, 30.6328f)
            curveTo(26.5137f, 31.4727f, 25.8203f, 32.1562f, 25.0098f, 32.1562f)
            curveTo(24.1699f, 32.1562f, 23.4766f, 31.4727f, 23.4863f, 30.6328f)
            close()
        }
    }.build()
}

@Preview
@Composable
private fun AlertIconPreview() {
    Box(
        modifier = Modifier
            .background(ChacColors.Background)
            .padding(16.dp)
    ) {
        Icon(
            imageVector = ChacIcons.Alert,
            contentDescription = null,
            tint = androidx.compose.ui.graphics.Color.Unspecified,
        )
    }
}
