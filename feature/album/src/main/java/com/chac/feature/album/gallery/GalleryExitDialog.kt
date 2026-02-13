package com.chac.feature.album.gallery

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.chac.core.designsystem.ui.icon.Alert
import com.chac.core.designsystem.ui.icon.ChacIcons
import com.chac.core.designsystem.ui.theme.ChacColors
import com.chac.core.designsystem.ui.theme.ChacTextStyles
import com.chac.core.designsystem.ui.theme.ChacTheme
import com.chac.core.resources.R

/**
 * 선택 상태에서 페이지 이탈을 확인하는 대화상자를 표시한다.
 *
 * @param onClickConfirm 확인 버튼 클릭 이벤트 콜백
 * @param onDismiss 대화상자 닫힘 이벤트 콜백 (ex. 취소 또는 바깥 영역 클릭)
 */
@Composable
internal fun GalleryExitDialog(
    modifier: Modifier = Modifier,
    onClickConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = ChacColors.BackgroundPopup,
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    imageVector = ChacIcons.Alert,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.gallery_exit_title),
                    style = ChacTextStyles.Headline02,
                    color = ChacColors.Text01,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.gallery_exit_message),
                    style = ChacTextStyles.Body,
                    color = ChacColors.Text03,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(30.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = onDismiss,
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
                            text = stringResource(R.string.gallery_exit_cancel),
                            style = ChacTextStyles.Btn,
                            color = ChacColors.Primary,
                        )
                    }
                    Button(
                        onClick = onClickConfirm,
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
                            text = stringResource(R.string.gallery_exit_confirm),
                            style = ChacTextStyles.Btn,
                            color = ChacColors.TextBtn01,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GalleryExitDialogPreview() {
    ChacTheme {
        GalleryExitDialog(
            onClickConfirm = {},
            onDismiss = {},
        )
    }
}
