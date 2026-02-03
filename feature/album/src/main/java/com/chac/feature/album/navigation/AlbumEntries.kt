package com.chac.feature.album.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.chac.feature.album.clustering.ClusteringRoute
import com.chac.feature.album.gallery.GalleryRoute
import com.chac.feature.album.model.MediaClusterUiModel
import com.chac.feature.album.save.SaveCompletedRoute

/**
 * 앨범 목적지를 Navigation3 entry provider에 등록한다
 *
 * @param onClickSavePartial '사진 정리하기' 버튼 클릭 이벤트 콜백
 * @param onSaveCompleted 저장 완료 이후 동작을 전달하는 콜백
 * @param onCloseSaveCompleted 저장 완료 화면 닫기 버튼 클릭 이벤트 콜백
 * @param onClickToGallery 저장 완료 화면에서 '갤러리로' 버튼 클릭 이벤트 콜백
 * @param onClickToList 저장 완료 화면에서 '목록으로' 버튼 클릭 이벤트 콜백
 * @param onClickBack 뒤로가기 버튼 클릭 이벤트 콜백
 */
fun EntryProviderScope<NavKey>.albumEntries(
    onClickSavePartial: (MediaClusterUiModel) -> Unit,
    onSaveCompleted: (String, Int) -> Unit,
    onCloseSaveCompleted: () -> Unit,
    onClickToGallery: () -> Unit,
    onClickToList: () -> Unit,
    onClickBack: () -> Unit,
) {
    entry(AlbumNavKey.Clustering) { _ ->
        ClusteringRoute(
            onClickSavePartial = onClickSavePartial,
        )
    }
    entry<AlbumNavKey.Gallery> { key ->
        GalleryRoute(
            cluster = key.cluster,
            onSaveCompleted = onSaveCompleted,
            onClickBack = onClickBack,
        )
    }
    entry<AlbumNavKey.SaveCompleted> { key ->
        SaveCompletedRoute(
            title = key.title,
            savedCount = key.savedCount,
            onClose = onCloseSaveCompleted,
            onClickToGallery = onClickToGallery,
            onClickToList = onClickToList,
        )
    }
}
