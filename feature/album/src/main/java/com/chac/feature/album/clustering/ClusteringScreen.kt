package com.chac.feature.album.clustering

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chac.core.designsystem.ui.theme.ChacTheme

/**
 * 클러스터링 화면 라우트
 *
 * @param onOpenGallery 갤러리로 이동하는 콜백
 * @param viewModel 클러스터링 화면 ViewModel
 */
@Composable
fun ClusteringRoute(
    onOpenGallery: (List<String>) -> Unit,
    viewModel: ClusteringViewModel = viewModel(),
) {
    ClusteringScreen(
        clusters = viewModel.clusters,
        onOpenGallery = onOpenGallery,
    )
}

/**
 * 클러스터링 목록 화면
 *
 * @param clusters 화면에 표시할 클러스터 목록
 * @param onOpenGallery 갤러리로 이동하는 콜백
 */
@Composable
private fun ClusteringScreen(
    clusters: List<ClusterItem>,
    onOpenGallery: (List<String>) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(text = "Clustering")
        clusters.forEach { cluster ->
            Button(onClick = { onOpenGallery(cluster.photos) }) {
                Text(text = cluster.title)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ClusteringScreenPreview() {
    ChacTheme {
        ClusteringScreen(
            clusters = listOf(
                ClusterItem(title = "Cluster A", photos = listOf("A-1", "A-2")),
                ClusterItem(title = "Cluster B", photos = listOf("B-1")),
            ),
            onOpenGallery = {},
        )
    }
}
