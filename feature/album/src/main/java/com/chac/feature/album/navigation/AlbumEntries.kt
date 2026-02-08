package com.chac.feature.album.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.chac.feature.album.clustering.ClusteringRoute
import com.chac.feature.album.gallery.GalleryAllPhotosRoute
import com.chac.feature.album.gallery.GalleryRoute
import com.chac.feature.album.gallery.component.MediaPreviewRoute
import com.chac.feature.album.save.SaveCompletedRoute
import com.chac.feature.album.settings.SettingsRoute

/**
 * 앨범 목적지를 Navigation3 entry provider에 등록한다
 *
 * @param onClickCluster 클러스터 카드 클릭 이벤트 콜백 (clusterId)
 * @param onClickAllPhotos "모든 사진" 클릭 이벤트 콜백
 * @param onLongClickMediaItem 미디어 아이템의 롱클릭 이벤트 콜백
 * @param onClickSettings 설정 화면 이동 콜백
 * @param onSaveCompleted 저장 완료 이후 동작을 전달하는 콜백
 * @param onCloseSaveCompleted 저장 완료 화면 닫기 버튼 클릭 이벤트 콜백
 * @param onClickToList 저장 완료 화면에서 '목록으로' 버튼 클릭 이벤트 콜백
 * @param onClickBack 뒤로가기 버튼 클릭 이벤트 콜백
 */
fun EntryProviderScope<NavKey>.albumEntries(
    onClickCluster: (Long) -> Unit,
    onClickAllPhotos: () -> Unit,
    onLongClickMediaItem: (Long, Long) -> Unit,
    onClickSettings: () -> Unit,
    onSaveCompleted: (String, Int) -> Unit,
    onCloseSaveCompleted: () -> Unit,
    onClickToList: () -> Unit,
    onClickBack: () -> Unit,
) {
    entry(AlbumNavKey.Clustering) { _ ->
        ClusteringRoute(
            onClickCluster = onClickCluster,
            onClickAllPhotos = onClickAllPhotos,
            onClickSettings = onClickSettings,
        )
    }
    entry(AlbumNavKey.AllPhotosGallery) { _ ->
        GalleryAllPhotosRoute(
            onSaveCompleted = onSaveCompleted,
            onLongClickMediaItem = onLongClickMediaItem,
            onClickBack = onClickBack,
        )
    }
    entry<AlbumNavKey.Gallery> { key ->
        GalleryRoute(
            clusterId = key.clusterId,
            onSaveCompleted = onSaveCompleted,
            onLongClickMediaItem = onLongClickMediaItem,
            onClickBack = onClickBack,
        )
    }
    entry<AlbumNavKey.MediaPreview> { key ->
        MediaPreviewRoute(
            clusterId = key.clusterId,
            mediaId = key.mediaId,
            onDismiss = onClickBack,
        )
    }
    entry<AlbumNavKey.SaveCompleted> { key ->
        SaveCompletedRoute(
            title = key.title,
            savedCount = key.savedCount,
            onClose = onCloseSaveCompleted,
            onClickToList = onClickToList,
        )
    }
    entry(AlbumNavKey.Settings) { _ ->
        SettingsRoute(
            onClickBack = onClickBack,
        )
    }
}
