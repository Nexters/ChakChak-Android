package com.chac.feature.album.save

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chac.core.designsystem.ui.icon.ChacIcons
import com.chac.core.designsystem.ui.icon.Close
import com.chac.core.designsystem.ui.theme.ChacColors
import com.chac.core.designsystem.ui.theme.ChacTextStyles
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
        onClickClose = onClose,
        onClickToGallery = onClickToGallery,
        onClickToList = onClickToList,
    )
}

/**
 * 앨범 저장 완료 화면을 표시한다
 *
 * @param title 저장된 앨범 제목
 * @param savedCount 저장된 사진 개수
 * @param onClickClose 닫기 버튼 클릭 이벤트 콜백
 * @param onClickToGallery '갤러리로' 버튼 클릭 이벤트 콜백
 * @param onClickToList '목록으로' 버튼 클릭 이벤트 콜백
 */
@Composable
private fun SaveCompletedScreen(
    title: String,
    savedCount: Int,
    onClickClose: () -> Unit,
    onClickToGallery: () -> Unit,
    onClickToList: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = ChacColors.Background)
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            IconButton(
                onClick = onClickClose,
                modifier = Modifier.offset(x = 12.dp), // IconButton로 인한 패딩만큼 오른쪽으로 이동
                colors = IconButtonColors(
                    containerColor = Color.Unspecified,
                    contentColor = ChacColors.Text01,
                    disabledContainerColor = Color.Unspecified,
                    disabledContentColor = Color.Unspecified,
                ),
            ) {
                Icon(
                    imageVector = ChacIcons.Close,
                    contentDescription = stringResource(R.string.save_completed_close_cd),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val topSpaceRatio = 0.2f

            Spacer(Modifier.weight(topSpaceRatio))

            Column(
                modifier = Modifier.weight(1f - topSpaceRatio),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.im_album_save_success),
                    contentDescription = null,
                    modifier = Modifier.offset(x = 7.dp), // 시각적인 중심점 보정
                )

                Spacer(modifier = Modifier.height(26.dp))

                Text(
                    text = stringResource(R.string.save_completed_title),
                    style = ChacTextStyles.Headline02,
                    color = ChacColors.Text01,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.save_completed_message, savedCount),
                    style = ChacTextStyles.Body,
                    color = ChacColors.Text03,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onClickToGallery,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChacColors.Sub04,
                    contentColor = ChacColors.TextBtn02,
                ),
            ) {
                Text(
                    text = stringResource(R.string.save_completed_sub_button_title),
                    style = ChacTextStyles.Btn,
                )
            }

            Button(
                onClick = onClickToList,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChacColors.Primary,
                    contentColor = ChacColors.TextBtn01,
                ),
            ) {
                Text(
                    text = stringResource(R.string.save_completed_main_button_title),
                    style = ChacTextStyles.Btn,
                )
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
