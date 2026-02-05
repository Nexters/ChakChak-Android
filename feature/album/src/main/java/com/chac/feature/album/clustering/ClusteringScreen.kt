package com.chac.feature.album.clustering

import android.Manifest
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.painterResource
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
import com.chac.core.designsystem.ui.component.ChacToast
import com.chac.core.designsystem.ui.component.ChacToastIcon
import com.chac.core.designsystem.ui.component.ChacToastState
import com.chac.core.designsystem.ui.component.rememberChacToastState
import com.chac.core.designsystem.ui.theme.ChacColors
import com.chac.core.designsystem.ui.theme.ChacTextStyles
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
import com.chac.feature.album.clustering.component.TotalPhotoSummary
import com.chac.feature.album.clustering.model.ClusteringUiState
import com.chac.feature.album.model.MediaClusterUiModel
import com.chac.feature.album.model.MediaUiModel
import com.chac.feature.album.model.SaveUiStatus

/**
 * 클러스터링 화면 라우트
 *
 * @param viewModel 클러스터링 화면 ViewModel
 * @param onClickSavePartial '사진 정리하기' 버튼 클릭 이벤트 콜백
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
    val toastState = rememberChacToastState()
    val savedToastMessage = stringResource(R.string.clustering_keep_saved_toast)

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

    LaunchedEffect(viewModel) {
        viewModel.saveCompletedEvents.collect {
            toastState.showToast()
        }
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
        toastState = toastState,
        toastMessage = savedToastMessage,
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
 * 클러스터링 목록 화면.
 *
 * @param uiState 클러스터링 화면 상태
 * @param toastState 토스트 노출 상태
 * @param toastMessage 토스트 메시지
 * @param onClickSavePartial '사진 정리하기' 버튼 클릭 이벤트 콜백
 * @param onClickSaveAll '그대로 저장' 버튼 클릭 이벤트 콜백
 */
@Composable
private fun ClusteringScreen(
    uiState: ClusteringUiState,
    toastState: ChacToastState,
    toastMessage: String,
    onClickSavePartial: (MediaClusterUiModel) -> Unit,
    onClickSaveAll: (MediaClusterUiModel) -> Unit,
) {
    val context = LocalContext.current
    val clusters = (uiState as? ClusteringUiState.WithClusters)?.clusters.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChacColors.Background)
            .padding(horizontal = 20.dp),
    ) {
        ClusteringTopBar()

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

                is ClusteringUiState.Loading -> {
                    CommonSectionOfPermissionGranted(
                        isGenerating = true,
                        clusters = clusters,
                    )

                    Box(Modifier.weight(1f)) {
                        if (clusters.isEmpty()) {
                            LoadingState()
                        } else {
                            ClusterList(
                                clusters = clusters,
                                onClickSavePartial = onClickSavePartial,
                                onClickSaveAll = onClickSaveAll,
                            )
                        }
                    }
                }

                is ClusteringUiState.Completed -> {
                    CommonSectionOfPermissionGranted(
                        isGenerating = false,
                        clusters = clusters,
                    )

                    Box(Modifier.weight(1f)) {
                        if (clusters.isEmpty()) {
                            EmptyState()
                        } else {
                            ClusterList(
                                clusters = clusters,
                                onClickSavePartial = onClickSavePartial,
                                onClickSaveAll = onClickSaveAll,
                            )
                        }
                    }
                }
            }
        }
    }

    ChacToast(
        state = toastState,
        text = toastMessage,
        textColor = ChacColors.Text01,
        icon = ChacToastIcon.Vector(Icons.Outlined.Info, tint = ChacColors.Text01),
    )
}

/**
 * 권한이 허용된 화면의 공통 영역
 *
 * @param isGenerating 클러스터 리스트가 로딩상태인지 여부
 * @param clusters 클러스터 리스트
 */
@Composable
private fun CommonSectionOfPermissionGranted(
    isGenerating: Boolean,
    clusters: List<MediaClusterUiModel>,
    modifier: Modifier = Modifier,
) {
    val totalCount = clusters.sumOf { it.mediaList.size }

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.clustering_top_title),
            style = ChacTextStyles.Headline01,
            color = ChacColors.Text01,
        )

        Spacer(modifier = Modifier.height(20.dp))

        TotalPhotoSummary(totalCount = totalCount)

        Spacer(modifier = Modifier.height(40.dp))

        AlbumSectionHeader(
            isGenerating = isGenerating,
            clusterCount = clusters.size,
        )

        Spacer(modifier = Modifier.height(16.dp))
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
            toastState = rememberChacToastState(),
            toastMessage = stringResource(R.string.clustering_keep_saved_toast),
            onClickSavePartial = {},
            onClickSaveAll = {},
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
            title = "Jeju Trip",
            mediaList = sampleMedia,
            thumbnailUriStrings = listOf(
                "content://sample/0",
                "content://sample/1",
            ),
            saveStatus = SaveUiStatus.Default,
        ),
        MediaClusterUiModel(
            id = 2L,
            title = "서초동",
            mediaList = sampleMedia,
            thumbnailUriStrings = listOf(
                "content://sample/0",
                "content://sample/1",
            ),
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
