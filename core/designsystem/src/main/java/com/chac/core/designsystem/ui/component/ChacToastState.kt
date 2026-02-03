package com.chac.core.designsystem.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ChacToast 상태.
 *
 * - showToast() 호출 시 잠깐 표시되었다가 자동으로 숨겨진다.
 */
class ChacToastState internal constructor(
    private val durationMillis: Long,
    private val scope: CoroutineScope,
) {
    private var job: Job? = null

    var visible: Boolean by mutableStateOf(false)
        private set

    fun showToast() {
        job?.cancel()
        visible = true
        job = scope.launch {
            delay(durationMillis)
            visible = false
        }
    }
}

/**
 * ChacToast 상태를 생성한다.
 *
 * @param durationMillis 노출 시간 (기본값: 시스템 토스트 SHORT 2000ms)
 */
@Composable
fun rememberChacToastState(
    durationMillis: Long = 2000L,
): ChacToastState {
    val scope = rememberCoroutineScope()
    return remember { ChacToastState(durationMillis, scope) }
}
