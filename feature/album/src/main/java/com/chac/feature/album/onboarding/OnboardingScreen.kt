package com.chac.feature.album.onboarding

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chac.core.designsystem.ui.theme.ChacColors
import com.chac.core.designsystem.ui.theme.ChacTextStyles
import com.chac.core.designsystem.ui.theme.ChacTheme
import com.chac.core.resources.R
import kotlinx.coroutines.launch

private const val ONBOARDING_PAGE_COUNT = 3

/**
 * 온보딩 화면 라우트
 *
 * @param onCompleted 온보딩 완료 콜백
 */
@Composable
fun OnboardingRoute(
    onCompleted: () -> Unit,
) {
    OnboardingScreen(onCompleted = onCompleted)
}

/**
 * 온보딩 화면
 *
 * @param onCompleted 온보딩 완료 콜백
 */
@Composable
private fun OnboardingScreen(
    onCompleted: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { ONBOARDING_PAGE_COUNT })
    val coroutineScope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == ONBOARDING_PAGE_COUNT - 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChacColors.Background)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TextButton(
            onClick = onCompleted,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(
                text = stringResource(R.string.onboarding_skip),
                style = ChacTextStyles.Body,
                color = ChacColors.Text03,
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) { page ->
            OnboardingPage(page = page)
        }

        Spacer(modifier = Modifier.height(32.dp))

        PageIndicator(pagerState = pagerState)

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (isLastPage) {
                    onCompleted()
                } else {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ChacColors.Primary,
                contentColor = ChacColors.TextBtn01,
            ),
        ) {
            Text(
                text = if (isLastPage) {
                    stringResource(R.string.onboarding_start)
                } else {
                    stringResource(R.string.onboarding_next)
                },
                style = ChacTextStyles.Btn,
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

/**
 * 온보딩 페이지 내용
 *
 * @param page 페이지 인덱스
 */
@Composable
private fun OnboardingPage(
    page: Int,
    modifier: Modifier = Modifier,
) {
    val (titleRes, descriptionRes) = when (page) {
        0 -> R.string.onboarding_page1_title to R.string.onboarding_page1_description
        1 -> R.string.onboarding_page2_title to R.string.onboarding_page2_description
        else -> R.string.onboarding_page3_title to R.string.onboarding_page3_description
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // TODO: 디자인 확정 후 이미지 교체
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(ChacColors.Text04Caption.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "${page + 1}",
                style = ChacTextStyles.Headline01,
                color = ChacColors.Text03,
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = stringResource(titleRes),
            style = ChacTextStyles.Headline01,
            color = ChacColors.Text01,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(descriptionRes),
            style = ChacTextStyles.Body,
            color = ChacColors.Text03,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * 페이지 인디케이터
 *
 * @param pagerState 페이저 상태
 */
@Composable
private fun PageIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(pagerState.pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == pagerState.currentPage) {
                            ChacColors.Primary
                        } else {
                            ChacColors.Text04Caption.copy(alpha = 0.3f)
                        },
                    ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    ChacTheme {
        OnboardingScreen(onCompleted = {})
    }
}
