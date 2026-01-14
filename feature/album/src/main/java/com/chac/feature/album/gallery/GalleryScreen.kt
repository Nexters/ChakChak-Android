package com.chac.feature.album.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chac.core.designsystem.ui.theme.ChacTheme

/**
 * 갤러리 화면 라우트
 *
 * @param photos 화면에 표시할 사진 목록
 * @param onBack 뒤로가기 동작을 전달하는 콜백
 * @param viewModel 갤러리 화면 ViewModel
 */
@Composable
fun GalleryRoute(
    photos: List<String>,
    onBack: () -> Unit,
    viewModel: GalleryViewModel = viewModel(factory = GalleryViewModel.provideFactory(photos)),
) {
    GalleryScreen(
        title = viewModel.title,
        photos = viewModel.photos,
        onBack = onBack,
    )
}

/**
 * 갤러리 화면
 *
 * @param title 화면 타이틀
 * @param photos 화면에 표시할 사진 목록
 * @param onBack 뒤로가기 동작을 전달하는 콜백
 */
@Composable
private fun GalleryScreen(
    title: String,
    photos: List<String>,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onBack) {
                Text(text = "Back")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title)
        }
        photos.forEach { photo ->
            Text(text = photo)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GalleryScreenPreview() {
    ChacTheme {
        GalleryScreen(
            title = "Gallery",
            photos = listOf("Photo 1", "Photo 2", "Photo 3"),
            onBack = {},
        )
    }
}
