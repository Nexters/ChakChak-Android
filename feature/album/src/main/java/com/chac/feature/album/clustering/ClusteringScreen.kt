package com.chac.feature.album.clustering

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.chac.core.designsystem.ui.theme.ChacTheme
import com.chac.core.permission.MediaWithLocationPermissionUtil.launchMediaWithLocationPermission
import com.chac.core.permission.compose.PermissionDeniedDialog
import com.chac.core.permission.compose.moveToPermissionSetting
import com.chac.core.permission.compose.rememberRegisterMediaWithLocationPermission

/**
 * 클러스터링 화면 라우트
 *
 * @param onOpenGallery 갤러리로 이동하는 콜백
 * @param viewModel 클러스터링 화면 ViewModel
 */
@Composable
fun ClusteringRoute(
    onOpenGallery: (List<String>) -> Unit,
    viewModel: ClusteringViewModel = hiltViewModel(),
) {
    val mediaState by viewModel.mediaState
    ClusteringScreen(
        clusters = viewModel.clusters,
        media = mediaState,
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
    media: List<MediaUiModel>,
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

        LazyColumn {
            items(items = media, key = { it.id }) {
                Text(text = "id:${it.id}, uri:${it.uriString}")
            }
        }

        PermissionSample()
    }
}

@Composable
fun PermissionSample() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        var showPermissionDeniedDialog by remember { mutableStateOf(false) }

        val permission =
            rememberRegisterMediaWithLocationPermission(
                onGranted = {},
                onPermanentlyDenied = { showPermissionDeniedDialog = true },
                onDenied = { showPermissionDeniedDialog = true },
            )

        Button(
            onClick = {
                permission.launchMediaWithLocationPermission()
            },
        ) {
            Text("권한요청")
        }

        if (showPermissionDeniedDialog) {
            val context = LocalContext.current
            PermissionDeniedDialog(
                title = "권한 필요",
                message = "메시지",
                onDismissRequest = {
                    showPermissionDeniedDialog = false
                },
                onPositiveClick = {
                    showPermissionDeniedDialog = false
                    moveToPermissionSetting(context)
                },
            )
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
            media = emptyList(),
            onOpenGallery = {},
        )
    }
}
