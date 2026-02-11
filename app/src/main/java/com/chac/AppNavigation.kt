package com.chac

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.ViewModelStoreNavEntryDecoratorDefaults
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.SaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.chac.feature.album.navigation.AlbumNavKey
import com.chac.feature.album.navigation.albumEntries

/** Navigation3 호스트를 구성하고 앨범 기능 entry provider를 연결한다 */
@Composable
fun ChacAppNavigation() {
    val backStack = rememberNavBackStack(AlbumNavKey.Clustering)
    val saveableStateHolder = rememberSaveableStateHolder()

    BackHandler(enabled = backStack.size > 1) {
        backStack.pop()
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.pop() },
            entryDecorators = listOf(
                SaveableStateHolderNavEntryDecorator(saveableStateHolder),
                rememberViewModelStoreNavEntryDecorator(
                    viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current),
                    removeViewModelStoreOnPop = ViewModelStoreNavEntryDecoratorDefaults.removeViewModelStoreOnPop(),
                ),
            ),
            modifier = Modifier.padding(innerPadding),
            entryProvider = entryProvider {
                albumEntries(
                    onClickCluster = { clusterId ->
                        backStack.add(AlbumNavKey.Gallery(clusterId))
                    },
                    onClickAllPhotos = {
                        backStack.add(AlbumNavKey.AllPhotosGallery)
                    },
                    onClickNextInGallery = { clusterId, selectedMediaIds ->
                        backStack.add(AlbumNavKey.AlbumTitleEdit(clusterId, selectedMediaIds))
                    },
                    onLongClickMediaItem = { clusterId, mediaId ->
                        backStack.add(
                            if (clusterId == null) {
                                AlbumNavKey.AllPhotosMediaPreview(mediaId)
                            } else {
                                AlbumNavKey.MediaPreview(clusterId, mediaId)
                            },
                        )
                    },
                    onClickSettings = {
                        backStack.add(AlbumNavKey.Settings)
                    },
                    onSaveCompleted = { title, savedCount ->
                        backStack.add(AlbumNavKey.SaveCompleted(title, savedCount))
                    },
                    onCloseSaveCompleted = { backStack.popToClustering() },
                    onClickToList = { backStack.popToClustering() },
                    onClickBack = { backStack.pop() },
                )
            },
        )
    }
}

/** 내비게이션 백스택을 pop 한다 */
private fun NavBackStack<NavKey>.pop() {
    if (size > 1) {
        removeLastOrNull()
    }
}

/** 클러스터링 화면까지 백스택을 정리한다 */
private fun NavBackStack<NavKey>.popToClustering() {
    while (size > 1 && lastOrNull() !is AlbumNavKey.Clustering) {
        removeLastOrNull()
    }
}
