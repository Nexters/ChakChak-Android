package com.chac

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
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
import kotlinx.serialization.Serializable

/** 앱 네비게이션 루트에서 사용하는 NavKey 정의 */
@Serializable
private sealed interface AppNavKey : NavKey {
    /** 앱 진입 화면 */
    @Serializable
    data object Home : AppNavKey
}

/** Navigation3 호스트를 구성하고 feature entry provider를 연결한다 */
@Composable
fun ChacAppNavigation() {
    val backStack = rememberNavBackStack(AppNavKey.Home)
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
                entry(AppNavKey.Home) {
                    HomeScreen()
                }
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

/** 앱 진입 시 표시되는 빈 화면. */
@Composable
private fun HomeScreen() {
    Box(modifier = Modifier.fillMaxSize())
}
