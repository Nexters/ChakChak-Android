package com.chac.feature.album.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.chac.feature.album.clustering.ClusteringRoute
import com.chac.feature.album.gallery.GalleryRoute

/**
 * 앨범 목적지를 Navigation3 entry provider에 등록한다
 *
 * @param onOpenGallery 클러스터링 목록에서 갤러리로 이동하는 콜백
 * @param onBack 갤러리에서 뒤로가기 동작을 전달하는 콜백
 */
fun EntryProviderScope<NavKey>.albumEntries(
    onOpenGallery: (List<String>) -> Unit,
    onBack: () -> Unit,
) {
    entry(AlbumNavKey.Clustering) { _ ->
        ClusteringRoute(onOpenGallery = onOpenGallery)
    }
    entry<AlbumNavKey.Gallery> { key ->
        GalleryRoute(
            photos = key.photos,
            onBack = onBack,
        )
    }
}
