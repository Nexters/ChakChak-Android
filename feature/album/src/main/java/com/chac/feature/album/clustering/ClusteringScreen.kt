package com.chac.feature.album.clustering

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.MediaStore
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chac.core.designsystem.ui.theme.ChacTheme
import com.chac.core.permission.MediaWithLocationPermissionUtil
import com.chac.core.permission.MediaWithLocationPermissionUtil.launchMediaWithLocationPermission
import com.chac.core.permission.compose.moveToPermissionSetting
import com.chac.core.permission.compose.rememberRegisterMediaWithLocationPermission
import com.chac.core.permission.compose.rememberWriteRequestLauncher
import com.chac.core.resources.R
import com.chac.domain.album.media.model.MediaType
import com.chac.feature.album.clustering.component.AlbumSectionHeader
import com.chac.feature.album.clustering.component.ClusterList
import com.chac.feature.album.clustering.component.ClusteringTopBar
import com.chac.feature.album.clustering.component.LoadingFooter
import com.chac.feature.album.clustering.component.PlaceholderIcon
import com.chac.feature.album.clustering.component.TotalPhotoSummary
import com.chac.feature.album.clustering.model.ClusteringUiState
import com.chac.feature.album.model.SaveUiStatus
import com.chac.feature.album.model.MediaClusterUiModel
import com.chac.feature.album.model.MediaUiModel

/**
 * 클러스터링 화면 라우트
 *
 * @param viewModel 클러스터링 화면 ViewModel
 * @param onClickSavePartial '사진 정리하기' 버튼 클릭 이벤트
 */
@Composable
fun ClusteringRoute(
    viewModel: ClusteringViewModel = hiltViewModel(),
    onClickSavePartial: (MediaClusterUiModel) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var pendingWriteCluster by remember { mutableStateOf<MediaClusterUiModel?>(null) }
    val permission = rememberRegisterMediaWithLocationPermission(
        onGranted = { viewModel.onPermissionChanged(true) },
        onDenied = { viewModel.onPermissionChanged(false) },
        onPermanentlyDenied = { viewModel.onPermissionChanged(false) },
    )
    val writeRequestLauncher = rememberWriteRequestLauncher(
        onGranted = {
            pendingWriteCluster?.let(viewModel::onClickSaveAll)
        },
        onDenied = { },
    )

    // 알림 권한 요청 런처
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            // TODO 여기다가 알림 권한 on off에 따른 ui 처리해두면됨
        },
    )

    LaunchedEffect(Unit) {
        val hasPermission = MediaWithLocationPermissionUtil.checkPermission(context)
        viewModel.onPermissionChanged(hasPermission)
        if (!hasPermission) {
            permission.launchMediaWithLocationPermission()
        }

        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onPermissionChanged(MediaWithLocationPermissionUtil.checkPermission(context))
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    ClusteringScreen(
        uiState = uiState,
        onClickSavePartial = onClickSavePartial,
        onClickSaveAll = { cluster ->
            if (cluster.mediaList.isEmpty()) return@ClusteringScreen

            val uris = cluster.mediaList.map { it.uriString.toUri() }
            val intentSender = MediaStore.createWriteRequest(
                context.contentResolver,
                uris,
            ).intentSender

            pendingWriteCluster = cluster
            writeRequestLauncher(intentSender)
        },
    )
}

/**
 * 클러스터링 목록 화면
 *
 * @param uiState 클러스터링 화면 상태
 * @param onClickSavePartial '사진 정리하기' 버튼 클릭 이벤트
 * @param onClickSaveAll '그대로 저장' 버튼 클릭 이벤트
 */
@Composable
private fun ClusteringScreen(
    uiState: ClusteringUiState,
    onClickSavePartial: (MediaClusterUiModel) -> Unit,
    onClickSaveAll: (MediaClusterUiModel) -> Unit,
) {
    val context = LocalContext.current

    val clusters = (uiState as? ClusteringUiState.WithClusters)?.clusters.orEmpty()
    val totalCount = clusters.sumOf { it.mediaList.size }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp),
    ) {
        ClusteringTopBar(label = stringResource(R.string.clustering_top_bar_label))
        Spacer(modifier = Modifier.height(16.dp))
        TotalPhotoSummary(totalCount = totalCount)
        Spacer(modifier = Modifier.height(16.dp))
        AlbumSectionHeader()
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when (uiState) {
                ClusteringUiState.PermissionChecking -> Unit

                ClusteringUiState.PermissionDenied -> {
                    PermissionRequiredState(
                        onOpenSettings = { moveToPermissionSetting(context) },
                    )
                }

                is ClusteringUiState.Loading -> {
                    if (clusters.isEmpty()) {
                        LoadingState()
                    } else {
                        ClusterList(
                            clusters = clusters,
                            isLoading = true,
                            onClickSavePartial = onClickSavePartial,
                            onClickSaveAll = onClickSaveAll,
                        )
                    }
                }

                is ClusteringUiState.Completed -> {
                    if (clusters.isEmpty()) {
                        EmptyState()
                    } else {
                        ClusterList(
                            clusters = clusters,
                            isLoading = false,
                            onClickSavePartial = onClickSavePartial,
                            onClickSaveAll = onClickSaveAll,
                        )
                    }
                }
            }
        }
    }
}

/** 로딩 상태 화면을 표시한다 */
@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        LoadingFooter()
    }
}

/** 빈 상태 화면을 표시한다 */
@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        PlaceholderIcon()
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.clustering_empty_title),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.clustering_empty_message),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * 권한이 필요할 때 표시되는 상태 화면을 보여준다
 *
 * @param onOpenSettings 설정 화면으로 이동하는 콜백
 */
@Composable
private fun PermissionRequiredState(
    onOpenSettings: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        PlaceholderIcon()
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.clustering_permission_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onOpenSettings) {
            Text(stringResource(R.string.clustering_permission_action))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ClusteringScreenPreview(
    @PreviewParameter(ClusteringUiStatePreviewProvider::class) uiState: ClusteringUiState,
) {
    ChacTheme {
        ClusteringScreen(
            uiState = uiState,
            onClickSavePartial = {},
            onClickSaveAll = {},
        )
    }
}

/** ClusteringScreen 프리뷰 상태를 제공한다 */
private class ClusteringUiStatePreviewProvider : PreviewParameterProvider<ClusteringUiState> {
    private val sampleMedia: List<MediaUiModel> = List(34) { index ->
        MediaUiModel(
            id = index.toLong(),
            uriString = "content://sample/$index",
            dateTaken = 0L,
            mediaType = MediaType.IMAGE,
        )
    }

    private val sampleClusters = listOf(
        MediaClusterUiModel(
            id = 1L,
            title = "Jeju Trip",
            mediaList = sampleMedia,
            saveStatus = SaveUiStatus.Default,
        ),
        MediaClusterUiModel(
            id = 2L,
            title = "서초동",
            mediaList = sampleMedia,
            saveStatus = SaveUiStatus.SaveCompleted,
        ),
    )

    override val values: Sequence<ClusteringUiState> = sequenceOf(
        ClusteringUiState.PermissionChecking,
        ClusteringUiState.Loading(emptyList()),
        ClusteringUiState.Loading(sampleClusters),
        ClusteringUiState.Completed(sampleClusters),
        ClusteringUiState.Completed(emptyList()),
        ClusteringUiState.PermissionDenied,
    )
}
