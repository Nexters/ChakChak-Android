package com.chac

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
fun ChacAppNavigation(
    viewModel: MainViewModel = hiltViewModel(),
) {
    val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()

    if (startDestination == StartDestination.Loading) return

    val initialKey: AlbumNavKey = when (startDestination) {
        StartDestination.Onboarding -> AlbumNavKey.Onboarding
        else -> AlbumNavKey.Clustering
    }

    ChacNavHost(
        initialKey = initialKey,
        onOnboardingCompleted = viewModel::completeOnboarding,
    )
}

@Composable
private fun ChacNavHost(
    initialKey: AlbumNavKey,
    onOnboardingCompleted: () -> Unit,
) {
    val backStack = rememberNavBackStack(initialKey)
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
                    onClickCluster = { cluster ->
                        backStack.add(AlbumNavKey.Gallery(cluster))
                    },
                    onClickMediaPreview = { cluster, mediaId ->
                        backStack.add(AlbumNavKey.MediaPreview(cluster, mediaId))
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
                    onOnboardingCompleted = {
                        onOnboardingCompleted()
                        backStack.replaceWith(AlbumNavKey.Clustering)
                    },
                    onClickOnboarding = {
                        backStack.add(AlbumNavKey.Onboarding)
                    },
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

/** 백스택을 지정된 키로 교체한다 */
private fun NavBackStack<NavKey>.replaceWith(key: NavKey) {
    clear()
    add(key)
}
