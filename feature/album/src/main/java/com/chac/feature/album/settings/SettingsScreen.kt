package com.chac.feature.album.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chac.core.designsystem.ui.icon.Back
import com.chac.core.designsystem.ui.icon.ChacIcons
import com.chac.core.designsystem.ui.theme.ChacColors
import com.chac.core.designsystem.ui.theme.ChacTextStyles
import com.chac.core.designsystem.ui.theme.ChacTheme
import com.chac.core.resources.R
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

private const val PRIVACY_POLICY_URL =
    "https://ojh102.notion.site/2ed107af0aeb80608ec3c5550dca41eb?pvs=74"

/**
 * 설정 화면 라우트
 *
 * @param onClickBack 뒤로가기 버튼 클릭 이벤트 콜백
 */
@Composable
fun SettingsRoute(
    onClickBack: () -> Unit,
) {
    val context = LocalContext.current

    SettingsScreen(
        onClickBack = onClickBack,
        onClickOssLicenses = {
            context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
        },
        onClickPrivacyPolicy = {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)))
        },
    )
}

/**
 * 설정 화면
 *
 * @param onClickBack 뒤로가기 버튼 클릭 이벤트 콜백
 * @param onClickOssLicenses 오픈소스 라이선스 클릭 이벤트 콜백
 * @param onClickPrivacyPolicy 개인정보 처리방침 클릭 이벤트 콜백
 */
@Composable
private fun SettingsScreen(
    onClickBack: () -> Unit,
    onClickOssLicenses: () -> Unit,
    onClickPrivacyPolicy: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChacColors.Background),
    ) {
        SettingsTopBar(onClickBack = onClickBack)

        SettingsItem(
            title = stringResource(R.string.settings_oss_licenses),
            onClick = onClickOssLicenses,
        )

        SettingsItem(
            title = stringResource(R.string.settings_privacy_policy),
            onClick = onClickPrivacyPolicy,
        )
    }
}

/**
 * 설정 화면 TopBar
 *
 * @param onClickBack 뒤로가기 버튼 클릭 이벤트 콜백
 */
@Composable
private fun SettingsTopBar(
    onClickBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(52.dp)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(
            onClick = onClickBack,
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            Icon(
                imageVector = ChacIcons.Back,
                contentDescription = stringResource(R.string.settings_back_cd),
                tint = ChacColors.Text01,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = stringResource(R.string.settings_title),
            style = ChacTextStyles.Title,
            color = ChacColors.Text01,
        )
    }
}

/**
 * 설정 항목
 *
 * @param title 항목 제목
 * @param onClick 클릭 이벤트 콜백
 */
@Composable
private fun SettingsItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = ChacTextStyles.Body,
            color = ChacColors.Text01,
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    ChacTheme {
        SettingsScreen(
            onClickBack = {},
            onClickOssLicenses = {},
            onClickPrivacyPolicy = {},
        )
    }
}
