package com.chac.core.designsystem.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chac.core.designsystem.ui.icon.Back
import com.chac.core.designsystem.ui.icon.ChacIcons
import com.chac.core.designsystem.ui.theme.ChacColors
import com.chac.core.designsystem.ui.theme.ChacTextStyles
import com.chac.core.designsystem.ui.theme.ChacTheme
import com.chac.core.resources.R as ResourcesR

/**
 * 앱 전역에서 사용하는 공통 TopBar 컴포넌트.
 *
 * @param title 중앙에 표시할 텍스트 제목
 * @param showWatermark 좌측에 워터마크 이미지를 노출할지 여부
 * @param navigationContentDescription 내비게이션 아이콘 접근성 설명
 * @param onClickBack 좌측 뒤로가기 아이콘 클릭 콜백 (null이면 아이콘 미표시)
 * @param actions 우측 영역에 표시할 액션 슬롯
 */
@Composable
fun ChacTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    showWatermark: Boolean = false,
    navigationContentDescription: String? = null,
    onClickBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (showWatermark) {
            Image(
                painter = painterResource(id = ResourcesR.drawable.ic_watermark),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(width = 64.dp, height = 22.dp),
            )
        }

        if (onClickBack != null) {
            IconButton(
                onClick = onClickBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-12).dp),
            ) {
                Icon(
                    imageVector = ChacIcons.Back,
                    contentDescription = navigationContentDescription,
                    tint = ChacColors.Text01,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        if (title != null) {
            Text(
                text = title,
                style = ChacTextStyles.Title,
                color = ChacColors.Text01,
            )
        }

        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            content = actions,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChacTopBarPreview() {
    ChacTheme {
        Column(
            modifier = Modifier
                .background(ChacColors.Background)
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            ChacTopBar(
                showWatermark = true,
                actions = {
                    IconButton(
                        onClick = {},
                        modifier = Modifier.offset(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = null,
                            tint = ChacColors.Text04Caption,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
            )

            Spacer(modifier = Modifier.height(12.dp))

            ChacTopBar(
                title = "사진 정리",
                navigationContentDescription = "뒤로가기",
                onClickBack = {},
            )
        }
    }
}
