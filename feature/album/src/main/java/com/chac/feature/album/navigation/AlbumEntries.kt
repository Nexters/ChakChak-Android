package com.chac.feature.album.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.chac.feature.album.clustering.ClusteringRoute
import com.chac.feature.album.gallery.AllPhotosGalleryRoute
import com.chac.feature.album.gallery.GalleryRoute
import com.chac.feature.album.gallery.component.AllPhotosMediaPreviewRoute
import com.chac.feature.album.gallery.component.MediaPreviewRoute
import com.chac.feature.album.save.AlbumTitleEditRoute
import com.chac.feature.album.save.SaveCompletedRoute
import com.chac.feature.album.settings.SettingsRoute

/**
 * 앨범 목적지를 Navigation3 entry provider에 등록한다
 *
 * @param onClickCluster 클러스터 카드 클릭 이벤트 콜백 (clusterId)
 * @param onClickAllPhotos '모든 사진' 버튼 클릭 이벤트 콜백
 * @param onClickNextInGallery 갤러리 화면에서 '다음' 버튼 클릭 이벤트 콜백 (clusterId, selectedMediaIds). 전체 사진 저장 시 clusterId는 null이다.
 * @param onLongClickMediaItem 미디어 아이템의 롱클릭 이벤트 콜백 (clusterId, mediaId). 전체 사진 모드에서는 clusterId가 null이다.
 * @param onClickSettings 설정 화면 이동 콜백
 * @param onSaveCompleted 저장 완료 이후 동작을 전달하는 콜백
 * @param onCloseSaveCompleted 저장 완료 화면 닫기 버튼 클릭 이벤트 콜백
 * @param onClickToList 저장 완료 화면에서 '목록으로' 버튼 클릭 이벤트 콜백
 * @param onClickBack 뒤로가기 버튼 클릭 이벤트 콜백
 */
fun EntryProviderScope<NavKey>.albumEntries(
    onClickCluster: (Long) -> Unit,
    onClickAllPhotos: () -> Unit,
    onClickNextInGallery: (Long?, List<Long>) -> Unit,
    onLongClickMediaItem: (Long?, Long) -> Unit,
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
        AllPhotosGalleryRoute(
            onClickNext = { onClickNextInGallery(null, it) },
            onLongClickMediaItem = onLongClickMediaItem,
            onClickBack = onClickBack,
        )
    }
    entry<AlbumNavKey.Gallery> { key ->
        GalleryRoute(
            clusterId = key.clusterId,
            onClickNext = { onClickNextInGallery(key.clusterId, it) },
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
    entry<AlbumNavKey.AllPhotosMediaPreview> { key ->
        AllPhotosMediaPreviewRoute(
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
    entry<AlbumNavKey.AlbumTitleEdit> { key ->
        AlbumTitleEditRoute(
            clusterId = key.clusterId,
            selectedMediaIds = key.selectedMediaIds,
            onSaveCompleted = onSaveCompleted,
            onClickBack = onClickBack,
        )
    }
    entry(AlbumNavKey.Settings) { _ ->
        SettingsRoute(
            onClickBack = onClickBack,
        )
    }
}
