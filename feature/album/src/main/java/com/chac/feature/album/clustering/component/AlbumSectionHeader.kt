package com.chac.feature.album.clustering.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chac.core.designsystem.ui.theme.ChacColors
import com.chac.core.designsystem.ui.theme.ChacTextStyles
import com.chac.core.designsystem.ui.theme.ChacTheme
import com.chac.core.resources.R
import kotlin.math.floor

/**
 * 앨범 생성 섹션의 헤더를 표시한다.
 *
 * 생성 중 상태에서는 점 애니메이션을, 완료 상태에서는 생성된 클러스터 수를 노출한다.
 *
 * @param isGenerating 현재 앨범 생성 진행 여부
 * @param clusterCount 생성된 클러스터 개수
 */
@Composable
fun AlbumSectionHeader(
    isGenerating: Boolean,
    clusterCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_album_section_header),
            contentDescription = null,
            tint = ChacColors.Text02,
            modifier = Modifier.size(16.dp),
        )

        Spacer(Modifier.width(4.dp))

        Text(
            text = stringResource(R.string.clustering_album_section_title),
            style = ChacTextStyles.SubTitle02,
            color = ChacColors.Text02,
        )

        Spacer(Modifier.width(8.dp))

        if (isGenerating) {
            AnimatedEllipsis()
        } else {
            Text(
                text = clusterCount.toString(),
                style = ChacTextStyles.Number,
                color = ChacColors.Sub01,
            )
        }
    }
}

/**
 * 헤더 우측에 표시되는 3개 점(ellipsis) 로딩 애니메이션을 그린다
 *
 * 각 점의 기본 크기는 4dp이며, 첫 번째 점부터 순차적으로 5dp로 커졌다가
 * 다음 점이 커질 때 이전 점은 다시 4dp로 돌아오도록 반복한다.
 *
 * @param modifier 컴포저블에 적용할 [Modifier]
 */
@Composable
private fun AnimatedEllipsis(
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "albumHeaderEllipsis")
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 900,
                easing = LinearEasing,
            ),
        ),
        label = "dotProgress",
    ).value
    val activeDotIndex = floor(progress).toInt().coerceIn(0, 2)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Dot(size = if (activeDotIndex == 0) 5.dp else 4.dp)
        Dot(size = if (activeDotIndex == 1) 5.dp else 4.dp)
        Dot(size = if (activeDotIndex == 2) 5.dp else 4.dp)
    }
}

/**
 * [AnimatedEllipsis]의 점
 *
 * @param size 점의 크기
 */
@Composable
private fun Dot(
    size: Dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color = ChacColors.Sub02),
    )
}

@Preview(showBackground = true)
@Composable
private fun AlbumSectionHeaderPreview(
    @PreviewParameter(AlbumSectionHeaderPreviewProvider::class) isGenerating: Boolean,
) {
    ChacTheme {
        Column(
            modifier = Modifier
                .background(color = ChacColors.Background)
                .padding(16.dp),
        ) {
            AlbumSectionHeader(
                isGenerating = isGenerating,
                clusterCount = 24,
            )
        }
    }
}

private class AlbumSectionHeaderPreviewProvider : PreviewParameterProvider<Boolean> {
    override val values: Sequence<Boolean> = sequenceOf(true, false)
}
