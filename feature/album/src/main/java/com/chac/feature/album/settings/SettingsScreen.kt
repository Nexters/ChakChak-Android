package com.chac.feature.album.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.chac.core.designsystem.ui.component.ChacTopBar
import com.chac.core.designsystem.ui.icon.ArrowRight
import com.chac.core.designsystem.ui.icon.ChacIcons
import com.chac.core.designsystem.ui.theme.ChacColors
import com.chac.core.designsystem.ui.theme.ChacTextStyles
import com.chac.core.designsystem.ui.theme.ChacTheme
import com.chac.core.resources.R
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

private const val PRIVACY_POLICY_URL = "https://ojh102.notion.site/2ed107af0aeb80608ec3c5550dca41eb?pvs=74"

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
        onClickRewatchOnboarding = {},
        onClickPrivacyPolicy = {
            context.startActivity(Intent(Intent.ACTION_VIEW, PRIVACY_POLICY_URL.toUri()))
        },
    )
}

/**
 * 설정 화면
 *
 * @param onClickBack 뒤로가기 버튼 클릭 이벤트 콜백
 * @param onClickOssLicenses 오픈소스 라이선스 클릭 이벤트 콜백
 * @param onClickRewatchOnboarding 온보딩 다시보기 클릭 이벤트 콜백
 * @param onClickPrivacyPolicy 개인정보 처리방침 클릭 이벤트 콜백
 */
@Composable
private fun SettingsScreen(
    onClickBack: () -> Unit,
    onClickOssLicenses: () -> Unit,
    onClickRewatchOnboarding: () -> Unit,
    onClickPrivacyPolicy: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChacColors.Background)
            .padding(horizontal = 20.dp),
    ) {
        ChacTopBar(
            title = stringResource(R.string.settings_title),
            navigationContentDescription = stringResource(R.string.settings_back_cd),
            onClickBack = onClickBack,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(color = ChacColors.Ffffff5)
                .padding(vertical = 8.dp),
        ) {
            SettingsItem(
                title = stringResource(R.string.settings_oss_licenses),
                onClick = onClickOssLicenses,
            )
            SettingsItem(
                title = stringResource(R.string.settings_rewatch_onboarding),
                onClick = onClickRewatchOnboarding,
            )
            SettingsItem(
                title = stringResource(R.string.settings_privacy_policy),
                onClick = onClickPrivacyPolicy,
            )
        }
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
            .padding(
                horizontal = 18.dp,
                vertical = 10.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = ChacTextStyles.Body,
            color = ChacColors.Text02,
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = ChacIcons.ArrowRight,
            contentDescription = null,
            modifier = Modifier
                .padding(end = 1.dp)
                .height(19.dp),
            tint = ChacColors.Text02,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    ChacTheme {
        SettingsScreen(
            onClickBack = {},
            onClickOssLicenses = {},
            onClickRewatchOnboarding = {},
            onClickPrivacyPolicy = {},
        )
    }
}
