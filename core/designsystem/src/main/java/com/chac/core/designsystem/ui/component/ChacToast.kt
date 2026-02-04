package com.chac.core.designsystem.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chac.core.designsystem.ui.theme.ChacColors
import com.chac.core.designsystem.ui.theme.ChacTextStyles
import com.chac.core.designsystem.ui.theme.ChacTheme

/**
 * ChacToast 아이콘 소스.
 *
 * - Vector: ImageVector 기반 아이콘
 * - Drawable: 리소스 ID 기반 아이콘
 */
sealed interface ChacToastIcon {
    /**
     * ImageVector 기반 아이콘.
     *
     * @property imageVector 표시할 벡터 아이콘
     * @property tint 아이콘 틴트 색상
     */
    data class Vector(
        val imageVector: ImageVector,
        val tint: Color = ChacColors.Text02,
    ) : ChacToastIcon

    /**
     * Drawable 리소스 기반 아이콘.
     *
     * @property resId drawable 리소스 ID
     * @property tint 아이콘 틴트 색상
     */
    data class Drawable(
        @DrawableRes val resId: Int,
        val tint: Color = ChacColors.Text02,
    ) : ChacToastIcon
}

/**
 * 하단 토스트 컴포넌트.
 *
 * - 키보드(IME)가 올라오면 자동으로 올라간다.
 * - 기본적으로 화면 하단에 고정되어 표시된다.
 *
 * @param state 토스트 상태
 * @param text 표시할 텍스트
 * @param modifier 컴포넌트 수정자
 * @param textColor 텍스트 색상
 * @param icon 아이콘 소스 (null이면 아이콘 미표시)
 * @param iconContentDescription 아이콘 접근성 설명
 * @param insets 하단 인셋 (네비게이션/IME 포함)
 */
@Composable
fun ChacToast(
    state: ChacToastState,
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = ChacColors.Text02,
    icon: ChacToastIcon? = ChacToastIcon.Vector(Icons.Outlined.Info),
    iconContentDescription: String? = null,
    insets: WindowInsets = WindowInsets.navigationBars.union(WindowInsets.ime),
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(insets.only(WindowInsetsSides.Bottom))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = state.visible,
            enter = slideInVertically { fullHeight -> fullHeight / 2 } + fadeIn(),
            exit = slideOutVertically { fullHeight -> fullHeight / 2 } + fadeOut(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color.Black.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (icon != null) {
                    when (icon) {
                        is ChacToastIcon.Vector -> {
                            Icon(
                                imageVector = icon.imageVector,
                                contentDescription = iconContentDescription,
                                modifier = Modifier.size(28.dp),
                                tint = icon.tint,
                            )
                        }

                        is ChacToastIcon.Drawable -> {
                            ChacImage(
                                model = icon.resId,
                                contentDescription = iconContentDescription,
                                modifier = Modifier.size(28.dp),
                                colorFilter = ColorFilter.tint(icon.tint),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.size(12.dp))
                }

                Text(
                    text = text,
                    style = ChacTextStyles.ToastBody,
                    color = textColor,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChacToastPreview() {
    ChacTheme {
        var text by remember { mutableStateOf("") }
        val toastState = rememberChacToastState()

        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            LaunchedEffect(Unit) {
                toastState.showToast()
            }

            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                placeholder = { Text(text = "메시지를 입력하세요") },
            )
            Button(
                onClick = {
                    toastState.showToast()
                },
                modifier = Modifier.align(Alignment.Center),
            ) {
                Text(text = "알림 표시")
            }
            ChacToast(
                text = text.ifBlank { "앨범이 갤러리에 저장되었습니다." },
                state = toastState,
            )
        }
    }
}
