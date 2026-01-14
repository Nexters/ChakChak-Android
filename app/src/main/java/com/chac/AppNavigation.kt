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
                    onOpenGallery = { photos -> backStack.add(AlbumNavKey.Gallery(photos)) },
                    onBack = { backStack.pop() },
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
