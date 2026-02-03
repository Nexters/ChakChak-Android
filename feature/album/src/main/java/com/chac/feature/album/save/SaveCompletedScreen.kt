package com.chac.feature.album.save

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chac.core.designsystem.ui.theme.ChacTheme
import com.chac.core.resources.R

/**
 * 앨범 저장 완료 화면 라우트
 *
 * @param title 저장된 앨범 제목
 * @param savedCount 저장된 사진 개수
 * @param onClose 닫기 버튼 클릭 이벤트 콜백
 * @param onClickToGallery '갤러리로' 버튼 클릭 이벤트 콜백
 * @param onClickToList '목록으로' 버튼 클릭 이벤트 콜백
 */
@Composable
fun SaveCompletedRoute(
    title: String,
    savedCount: Int,
    onClose: () -> Unit,
    onClickToGallery: () -> Unit,
    onClickToList: () -> Unit,
) {
    SaveCompletedScreen(
        title = title,
        savedCount = savedCount,
        onClose = onClose,
        onClickToGallery = onClickToGallery,
        onClickToList = onClickToList,
    )
}

/**
 * 앨범 저장 완료 화면을 표시한다
 *
 * @param title 저장된 앨범 제목
 * @param savedCount 저장된 사진 개수
 * @param onClose 닫기 버튼 클릭 이벤트 콜백
 * @param onClickToGallery '갤러리로' 버튼 클릭 이벤트 콜백
 * @param onClickToList '목록으로' 버튼 클릭 이벤트 콜백
 */
@Composable
private fun SaveCompletedScreen(
    title: String,
    savedCount: Int,
    onClose: () -> Unit,
    onClickToGallery: () -> Unit,
    onClickToList: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.save_completed_close_cd),
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                        ),
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.save_completed_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.save_completed_message, savedCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onClickToGallery,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Text(text = stringResource(R.string.save_completed_gallery_action))
                }
                Button(
                    onClick = onClickToList,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Text(text = stringResource(R.string.save_completed_list_action))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SaveCompletedScreenPreview() {
    ChacTheme {
        SaveCompletedRoute(
            title = "Jeju Trip",
            savedCount = 12,
            onClose = {},
            onClickToGallery = {},
            onClickToList = {},
        )
    }
}
