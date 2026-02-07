package com.chac.feature.album.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.chac.feature.album.clustering.ClusteringRoute
import com.chac.feature.album.gallery.GalleryRoute
import com.chac.feature.album.gallery.component.MediaPreviewRoute
import com.chac.feature.album.model.MediaClusterUiModel
import com.chac.feature.album.onboarding.OnboardingRoute
import com.chac.feature.album.save.SaveCompletedRoute
import com.chac.feature.album.settings.SettingsRoute

/**
 * 앨범 목적지를 Navigation3 entry provider에 등록한다
 *
 * @param onClickCluster 클러스터 카드 클릭 이벤트 콜백
 * @param onClickMediaPreview 미디어 미리보기 화면 이동 콜백
 * @param onClickSettings 설정 화면 이동 콜백
 * @param onSaveCompleted 저장 완료 이후 동작을 전달하는 콜백
 * @param onCloseSaveCompleted 저장 완료 화면 닫기 버튼 클릭 이벤트 콜백
 * @param onClickToList 저장 완료 화면에서 '목록으로' 버튼 클릭 이벤트 콜백
 * @param onClickBack 뒤로가기 버튼 클릭 이벤트 콜백
 * @param onOnboardingCompleted 온보딩 완료 콜백
 * @param onClickOnboarding 온보딩 화면 이동 콜백
 */
fun EntryProviderScope<NavKey>.albumEntries(
    onClickCluster: (MediaClusterUiModel) -> Unit,
    onClickMediaPreview: (MediaClusterUiModel, Long) -> Unit,
    onClickSettings: () -> Unit,
    onSaveCompleted: (String, Int) -> Unit,
    onCloseSaveCompleted: () -> Unit,
    onClickToList: () -> Unit,
    onClickBack: () -> Unit,
    onOnboardingCompleted: () -> Unit,
    onClickOnboarding: () -> Unit,
) {
    entry(AlbumNavKey.Clustering) { _ ->
        ClusteringRoute(
            onClickCluster = onClickCluster,
            onClickSettings = onClickSettings,
        )
    }
    entry<AlbumNavKey.Gallery> { key ->
        GalleryRoute(
            cluster = key.cluster,
            onSaveCompleted = onSaveCompleted,
            onClickMediaPreview = onClickMediaPreview,
            onClickBack = onClickBack,
        )
    }
    entry<AlbumNavKey.MediaPreview> { key ->
        MediaPreviewRoute(
            cluster = key.cluster,
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
            onClickOnboarding = onClickOnboarding,
        )
    }
    entry(AlbumNavKey.Onboarding) { _ ->
        OnboardingRoute(
            onCompleted = onOnboardingCompleted,
        )
    }
}
