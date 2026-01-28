package com.chac.feature.album.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.chac.feature.album.clustering.ClusteringRoute
import com.chac.feature.album.gallery.GalleryRoute
import com.chac.feature.album.model.ClusterUiModel
import com.chac.feature.album.save.SaveCompletedRoute

/**
 * 앨범 목적지를 Navigation3 entry provider에 등록한다
 *
 * @param onOpenGallery 클러스터링 목록에서 갤러리로 이동하는 콜백
 * @param onOpenSaveCompleted 저장 완료 화면으로 이동하는 콜백
 * @param onCloseSaveCompleted 저장 완료 화면 닫기 콜백
 * @param onPopToGallery 저장 완료 화면에서 갤러리로 이동하는 콜백
 * @param onPopToClustering 저장 완료 화면에서 클러스터링 목록으로 이동하는 콜백
 * @param onBack 갤러리에서 뒤로가기 동작을 전달하는 콜백
 */
fun EntryProviderScope<NavKey>.albumEntries(
    onOpenGallery: (ClusterUiModel) -> Unit,
    onOpenSaveCompleted: (String, Int) -> Unit,
    onCloseSaveCompleted: () -> Unit,
    onPopToGallery: () -> Unit,
    onPopToClustering: () -> Unit,
    onBack: () -> Unit,
) {
    entry(AlbumNavKey.Clustering) { _ ->
        ClusteringRoute(
            onClickSavePartial = onOpenGallery,
        )
    }
    entry<AlbumNavKey.Gallery> { key ->
        GalleryRoute(
            cluster = key.cluster,
            onSaveCompleted = onOpenSaveCompleted,
            onBack = onBack,
        )
    }
    entry<AlbumNavKey.SaveCompleted> { key ->
        SaveCompletedRoute(
            title = key.title,
            savedCount = key.savedCount,
            onClose = onCloseSaveCompleted,
            onPopToGallery = onPopToGallery,
            onPopToList = onPopToClustering,
        )
    }
}
