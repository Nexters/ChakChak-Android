package com.chac.feature.album.gallery.component

import android.text.format.DateUtils
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chac.core.designsystem.ui.component.ChacImage
import com.chac.core.designsystem.ui.icon.ChacIcons
import com.chac.core.designsystem.ui.icon.Close
import com.chac.core.designsystem.ui.theme.ChacColors
import com.chac.core.designsystem.ui.theme.ChacTextStyles
import com.chac.core.designsystem.ui.theme.ChacTheme
import com.chac.core.resources.R
import com.chac.domain.album.media.model.MediaType
import com.chac.feature.album.model.MediaUiModel

/**
 * 미디어 미리보기 화면 라우트
 *
 * @param clusterId 클러스터 ID
 * @param mediaId 최초 표시할 미디어 식별자
 * @param viewModel 미리보기 화면 뷰모델
 * @param onDismiss 닫기 콜백
 */
@Composable
fun MediaPreviewRoute(
    clusterId: Long,
    mediaId: Long,
    viewModel: MediaPreviewViewModel = hiltViewModel(),
    onDismiss: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel, clusterId, mediaId) {
        viewModel.initialize(clusterId, mediaId)
    }

    MediaPreviewRouteScreen(
        uiState = uiState,
        onDismiss = onDismiss,
        headerForReady = { it.address },
    )
}

/**
 * 전체 사진 미디어 미리보기 화면 라우트
 *
 * @param mediaId 최초 표시할 미디어 식별자
 * @param viewModel 미리보기 화면 뷰모델
 * @param onDismiss 닫기 콜백
 */
@Composable
fun AllPhotosMediaPreviewRoute(
    mediaId: Long,
    viewModel: MediaPreviewViewModel = hiltViewModel(),
    onDismiss: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val header = stringResource(R.string.clustering_total_photo_title)

    LaunchedEffect(viewModel, mediaId) {
        viewModel.initializeAllPhotos(mediaId)
    }

    MediaPreviewRouteScreen(
        uiState = uiState,
        onDismiss = onDismiss,
        headerForReady = { header },
    )
}

@Composable
private fun MediaPreviewRouteScreen(
    uiState: MediaPreviewUiState,
    onDismiss: () -> Unit,
    headerForReady: (MediaPreviewUiState.Ready) -> String,
) {
    when (uiState) {
        is MediaPreviewUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ChacColors.Background),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = ChacColors.Primary)
            }
        }

        is MediaPreviewUiState.Ready -> {
            MediaPreviewScreen(
                mediaList = uiState.mediaList,
                initialIndex = uiState.initialIndex,
                address = headerForReady(uiState),
                onDismiss = onDismiss,
            )
        }
    }
}

/**
 * 전체화면 이미지 미리보기 화면
 *
 * 갤러리에서 사진 롱클릭 시 표시되며, 좌우 스와이프로 같은 클러스터 내 다른 사진으로 이동할 수 있다.
 *
 * @param mediaList 클러스터 내 미디어 목록
 * @param initialIndex 최초 표시할 미디어 인덱스
 * @param address 클러스터 주소
 * @param onDismiss 닫기 콜백
 */
@Composable
private fun MediaPreviewScreen(
    mediaList: List<MediaUiModel>,
    initialIndex: Int,
    address: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { mediaList.size },
    )

    val currentMedia by remember {
        derivedStateOf { mediaList[pagerState.currentPage] }
    }

    BackHandler(onBack = onDismiss)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChacColors.Background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .padding(start = 20.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = address,
                    style = ChacTextStyles.SubTitle03,
                    color = ChacColors.Text02,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = DateUtils.formatDateTime(
                        context,
                        currentMedia.dateTaken,
                        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME,
                    ),
                    style = ChacTextStyles.Caption,
                    color = ChacColors.Text04Caption,
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = ChacIcons.Close,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = ChacColors.Text01,
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) { page ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                ChacImage(
                    model = mediaList[page].uriString,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
        }

        Spacer(modifier = Modifier.padding(bottom = 20.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun MediaPreviewScreenPreview() {
    ChacTheme {
        MediaPreviewScreen(
            mediaList = List(10) { index ->
                MediaUiModel(
                    id = index.toLong(),
                    uriString = "content://sample/$index",
                    dateTaken = 1_700_000_000_000L,
                    mediaType = MediaType.IMAGE,
                )
            },
            initialIndex = 0,
            address = "제주특별자치도 제주시",
            onDismiss = {},
        )
    }
}
