package com.chac.feature.album.clustering

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chac.core.designsystem.ui.component.ChacTopBar
import com.chac.core.designsystem.ui.theme.ChacColors
import com.chac.core.designsystem.ui.theme.ChacTextStyles
import com.chac.core.designsystem.ui.theme.ChacTheme
import com.chac.core.permission.MediaWithLocationPermissionUtil
import com.chac.core.permission.MediaWithLocationPermissionUtil.launchMediaWithLocationPermission
import com.chac.core.permission.compose.moveToPermissionSetting
import com.chac.core.permission.compose.rememberAwaitNotificationPermissionResult
import com.chac.core.permission.compose.rememberRegisterMediaWithLocationPermission
import com.chac.core.resources.R
import com.chac.domain.album.media.model.MediaType
import com.chac.feature.album.clustering.component.AlbumSectionHeader
import com.chac.feature.album.clustering.component.ClusterList
import com.chac.feature.album.clustering.component.TotalPhotoSummary
import com.chac.feature.album.clustering.model.ClusteringUiState
import com.chac.feature.album.model.MediaClusterUiModel
import com.chac.feature.album.model.MediaUiModel

/**
 * 클러스터링 화면 라우트
 *
 * @param viewModel 클러스터링 화면 ViewModel
 * @param onClickCluster 클러스터 카드 클릭 이벤트 콜백
 * @param onClickAllPhotos 전체 사진 갤러리 이동 콜백
 * @param onClickSettings 설정 버튼 클릭 이벤트 콜백
 */
@Composable
fun ClusteringRoute(
    viewModel: ClusteringViewModel = hiltViewModel(),
    onClickCluster: (Long) -> Unit,
    onClickAllPhotos: () -> Unit,
    onClickSettings: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mediaWithLocationPermission = rememberRegisterMediaWithLocationPermission(
        onGranted = { viewModel.onMediaWithLocationPermissionChanged(true) },
        onDenied = { viewModel.onMediaWithLocationPermissionChanged(false) },
        onPermanentlyDenied = { viewModel.onMediaWithLocationPermissionChanged(false) },
    )

    val awaitNotificationPermissionResult = rememberAwaitNotificationPermissionResult()

    // 알림 권한 요청을 기다린 뒤, 미디어/위치 권한 요청을 진행한다.
    LaunchedEffect(Unit) {
        awaitNotificationPermissionResult()

        val hasMediaWithLocationPermission = MediaWithLocationPermissionUtil.checkPermission(context)
        viewModel.onMediaWithLocationPermissionChanged(hasMediaWithLocationPermission)
        if (!hasMediaWithLocationPermission) {
            mediaWithLocationPermission.launchMediaWithLocationPermission()
        }
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onMediaWithLocationPermissionChanged(MediaWithLocationPermissionUtil.checkPermission(context))
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    ClusteringScreen(
        uiState = uiState,
        onClickCluster = onClickCluster,
        onClickAllPhotos = onClickAllPhotos,
        onClickSettings = onClickSettings,
    )
}

/**
 * 클러스터링 목록 화면.
 *
 * @param uiState 클러스터링 화면 상태
 * @param onClickCluster 클러스터 카드 클릭 이벤트 콜백
 * @param onClickAllPhotos 전체 사진 갤러리 이동 콜백
 * @param onClickSettings 설정 버튼 클릭 이벤트 콜백
 */
@Composable
private fun ClusteringScreen(
    uiState: ClusteringUiState,
    onClickCluster: (Long) -> Unit,
    onClickAllPhotos: () -> Unit,
    onClickSettings: () -> Unit = {},
) {
    val context = LocalContext.current
    val clusters = (uiState as? ClusteringUiState.WithClusters)?.clusters.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChacColors.Background)
            .padding(horizontal = 20.dp),
    ) {
        ChacTopBar(
            showWatermark = true,
            actions = {
                IconButton(
                    onClick = onClickSettings,
                    modifier = Modifier.offset(x = 12.dp), // IconButton로 인한 패딩만큼 오른쪽으로 이동
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.clustering_settings_cd),
                        modifier = Modifier.size(24.dp),
                        tint = ChacColors.Text04Caption,
                    )
                }
            },
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            when (uiState) {
                ClusteringUiState.PermissionChecking -> Unit

                ClusteringUiState.PermissionDenied -> {
                    PermissionRequiredState { moveToPermissionSetting(context) }
                }

                is ClusteringUiState.WithClusters -> {
                    val isGenerating = uiState is ClusteringUiState.Loading

                    CommonSectionOfPermissionGranted(
                        isGenerating = isGenerating,
                        totalPhotoCount = uiState.totalPhotoCount,
                        clusters = clusters,
                        onClickAllPhotos = onClickAllPhotos,
                    )

                    Box(Modifier.weight(1f)) {
                        if (clusters.isEmpty()) {
                            if (isGenerating) {
                                LoadingState()
                            } else {
                                EmptyState()
                            }
                        } else {
                            ClusterList(
                                clusters = clusters,
                                onClickCluster = onClickCluster,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 권한이 허용된 화면의 공통 영역
 *
 * @param isGenerating 클러스터 리스트가 로딩상태인지 여부
 * @param totalPhotoCount 전체 사진 개수
 * @param clusters 클러스터 리스트
 * @param onClickAllPhotos 전체 사진 요약 카드 클릭 이벤트 콜백
 */
@Composable
private fun CommonSectionOfPermissionGranted(
    isGenerating: Boolean,
    totalPhotoCount: Int,
    clusters: List<MediaClusterUiModel>,
    onClickAllPhotos: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.clustering_top_title),
            style = ChacTextStyles.Headline01,
            color = ChacColors.Text01,
        )

        Spacer(modifier = Modifier.height(20.dp))

        TotalPhotoSummary(
            totalCount = totalPhotoCount,
            onClick = onClickAllPhotos,
        )

        Spacer(modifier = Modifier.height(40.dp))

        AlbumSectionHeader(
            isGenerating = isGenerating,
            clusterCount = clusters.size,
        )

        Spacer(modifier = Modifier.height(6.dp))
    }
}

/** 로딩 상태 화면을 표시한다. */
@Composable
private fun LoadingState() {
    Column(modifier = Modifier.fillMaxSize()) {
        val topSpaceRatio = 0.1f

        Spacer(Modifier.weight(topSpaceRatio))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f - topSpaceRatio),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = R.drawable.im_album_section_body_loading),
                contentDescription = null,
            )

            Spacer(Modifier.height(24.5.dp))

            Text(
                text = stringResource(R.string.clustering_loading_message),
                style = ChacTextStyles.Body,
                color = ChacColors.Text03,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** 빈 상태 화면을 표시한다. */
@Composable
private fun EmptyState() {
    Column(modifier = Modifier.fillMaxSize()) {
        val topSpaceRatio = 0.15f

        Spacer(Modifier.weight(topSpaceRatio))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f - topSpaceRatio),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = R.drawable.im_album_section_body_empty),
                contentDescription = null,
                modifier = Modifier.offset(x = 5.07.dp), // 시각적인 중앙값 보정
            )

            Spacer(modifier = Modifier.height(31.dp))

            Text(
                text = stringResource(R.string.clustering_empty_message),
                style = ChacTextStyles.Body,
                color = ChacColors.Text03,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * 권한이 필요할 때 표시되는 상태 화면을 보여준다.
 *
 * @param onOpenSettings 설정 화면으로 이동하는 콜백
 */
@Composable
private fun PermissionRequiredState(
    onOpenSettings: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        val topSpaceRatio = 0.35f

        Spacer(Modifier.weight(topSpaceRatio))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f - topSpaceRatio),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.clustering_permission_message),
                style = ChacTextStyles.Body,
                color = ChacColors.Text03,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onOpenSettings,
                modifier = Modifier
                    .width(154.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChacColors.Primary,
                    contentColor = ChacColors.TextBtn01,
                ),
            ) {
                Text(
                    text = stringResource(R.string.clustering_permission_action),
                    style = ChacTextStyles.Btn,
                )
            }
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
            onClickCluster = {},
            onClickAllPhotos = {},
        )
    }
}

/** ClusteringScreen 프리뷰 상태를 제공한다. */
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
            address = "Jeju Trip",
            formattedDate = "2024.01.15",
            mediaList = sampleMedia,
            thumbnailUriStrings = listOf(
                "content://sample/0",
                "content://sample/1",
            ),
        ),
        MediaClusterUiModel(
            id = 2L,
            address = "서초동",
            formattedDate = "2024.03.10",
            mediaList = sampleMedia,
            thumbnailUriStrings = listOf(
                "content://sample/0",
                "content://sample/1",
            ),
        ),
    )

    override val values: Sequence<ClusteringUiState> = sequenceOf(
        ClusteringUiState.PermissionChecking,
        ClusteringUiState.Loading(totalPhotoCount = 0, clusters = emptyList()),
        ClusteringUiState.Loading(totalPhotoCount = 34, clusters = sampleClusters),
        ClusteringUiState.Completed(totalPhotoCount = 34, clusters = sampleClusters),
        ClusteringUiState.Completed(totalPhotoCount = 0, clusters = emptyList()),
        ClusteringUiState.PermissionDenied,
    )
}
