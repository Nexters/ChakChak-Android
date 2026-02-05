package com.chac.feature.album.clustering.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chac.core.designsystem.ui.theme.ChacColors
import com.chac.core.designsystem.ui.theme.ChacTheme

/**
 * 클러스터링 화면 TopBar
 *
 * @param onClickSettings 설정 버튼 클릭 이벤트 콜백
 */
@Composable
fun ClusteringTopBar(
    modifier: Modifier = Modifier,
    onClickSettings: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        IconButton(
            onClick = onClickSettings,
            modifier = Modifier.offset(x = 12.dp), // IconButton로 인한 패딩만큼 오른쪽으로 이동
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = ChacColors.Text04Caption,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ClusteringTopBarPreview() {
    ChacTheme {
        Box(
            modifier = Modifier
                .background(color = ChacColors.Background)
                .padding(horizontal = 20.dp),
        ) {
            ClusteringTopBar()
        }
    }
}
