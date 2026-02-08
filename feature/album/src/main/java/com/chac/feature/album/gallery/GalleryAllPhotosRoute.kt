package com.chac.feature.album.gallery

import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chac.core.permission.compose.rememberWriteRequestLauncher

/**
 * "모든 사진" 갤러리 화면 라우트
 *
 * @param viewModel 갤러리 화면의 뷰모델
 * @param onSaveCompleted 저장 완료 이후 동작을 전달하는 콜백
 * @param onLongClickMediaItem 미디어 아이템의 롱클릭 이벤트 콜백
 * @param onClickBack 뒤로가기 버튼 클릭 이벤트 콜백
 */
@Composable
fun GalleryAllPhotosRoute(
    viewModel: GalleryViewModel = hiltViewModel(),
    onSaveCompleted: (String, Int) -> Unit,
    onLongClickMediaItem: (Long, Long) -> Unit,
    onClickBack: () -> Unit,
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    val writeRequestLauncher = rememberWriteRequestLauncher(
        onGranted = { viewModel.saveSelectedMedia() },
    )

    LaunchedEffect(viewModel) {
        viewModel.initializeAllPhotos()

        viewModel.saveCompletedEvents.collect { event ->
            onSaveCompleted(event.title, event.savedCount)
        }
    }

    GalleryScreen(
        uiState = uiState,
        clusterId = uiState.cluster.id,
        onToggleMedia = viewModel::toggleSelection,
        onClickSelectAll = { selected: Boolean ->
            if (selected) {
                viewModel.selectAll()
            } else {
                viewModel.clearSelection()
            }
        },
        onClickSave = {
            val selectedMediaList = viewModel.getSelectedMediaList()
            if (selectedMediaList.isEmpty()) return@GalleryScreen

            val uris = selectedMediaList.map { it.uriString.toUri() }
            val intentSender = MediaStore.createWriteRequest(
                context.contentResolver,
                uris,
            ).intentSender

            writeRequestLauncher(intentSender)
        },
        onLongClickMediaItem = { _, mediaId ->
            onLongClickMediaItem(uiState.cluster.id, mediaId)
        },
        onClickBack = onClickBack,
    )
}

