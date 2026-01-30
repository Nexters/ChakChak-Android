package com.chac.core.designsystem.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import coil.compose.AsyncImage

/**
 * 이미지 컴포저블 Wapper
 *
 * @param model 이미지 로드에 사용할 모델 데이터
 * @param contentDescription 접근성 설명
 * @param alignment 이미지 정렬 기준
 * @param contentScale 이미지 스케일링 규칙
 * @param alpha 이미지 투명도
 * @param colorFilter 색상 필터
 * @param filterQuality 스케일링 품질
 * @param placeholder 로딩 중 표시할 페인터
 * @param error 로딩 실패 시 표시할 페인터
 * @param fallback 모델이 null인 경우 표시할 페인터
 * @param onState 상태 변경 콜백
 * @param onLoading 로딩 시작 콜백
 * @param onSuccess 로딩 성공 콜백
 * @param onError 로딩 실패 콜백
 */
@Composable
fun ChacImage(
    model: Any?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Crop,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = FilterQuality.Low,
    placeholder: Painter? = null,
    error: Painter? = null,
    fallback: Painter? = null,
    onState: ((ChacImageState) -> Unit)? = null,
    onLoading: (() -> Unit)? = null,
    onSuccess: (() -> Unit)? = null,
    onError: ((Throwable?) -> Unit)? = null,
) {
    val onStateUpdated by rememberUpdatedState(onState)
    val onLoadingUpdated by rememberUpdatedState(onLoading)
    val onSuccessUpdated by rememberUpdatedState(onSuccess)
    val onErrorUpdated by rememberUpdatedState(onError)

    LaunchedEffect(model) {
        if (model == null) {
            onStateUpdated?.invoke(ChacImageState.Empty)
        }
    }

    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
        filterQuality = filterQuality,
        placeholder = placeholder,
        error = error,
        fallback = fallback,
        onLoading = {
            onStateUpdated?.invoke(ChacImageState.Loading)
            onLoadingUpdated?.invoke()
        },
        onSuccess = {
            onStateUpdated?.invoke(ChacImageState.Success)
            onSuccessUpdated?.invoke()
        },
        onError = { state ->
            val throwable = state.result.throwable
            onStateUpdated?.invoke(ChacImageState.Error(throwable))
            onErrorUpdated?.invoke(throwable)
        },
    )
}

sealed interface ChacImageState {
    data object Empty : ChacImageState

    data object Loading : ChacImageState

    data object Success : ChacImageState

    data class Error(val throwable: Throwable?) : ChacImageState
}
