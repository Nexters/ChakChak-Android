package com.chac.core.designsystem.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.chac.core.designsystem.R

private val Pretendard: FontFamily = FontFamily(
    Font(R.font.pretendard_regular, weight = FontWeight.Normal),
    Font(R.font.pretendard_medium, weight = FontWeight.Medium),
    Font(R.font.pretendard_semibold, weight = FontWeight.SemiBold),
    Font(R.font.pretendard_bold, weight = FontWeight.Bold),
)

private val Montserrat: FontFamily = FontFamily(
    Font(R.font.montserrat_semibold, weight = FontWeight.SemiBold),
)

object ChacTextStyles {
    val Headline01 = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    )

    val Headline02 = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Bold, // 700
        fontSize = 18.sp,
        lineHeight = 21.6.sp,
        letterSpacing = 0.sp,
    )

    val Title = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium, // 500
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    )

    val SubTitle01 = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium, // 500
        fontSize = 18.sp,
        lineHeight = 21.6.sp,
        letterSpacing = 0.sp,
    )

    val SubTitle02 = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Bold, // 700
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    )

    val SubTitle03 = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium, // 500
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
    )

    val ContentTitle = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium, // 500
        fontSize = 16.sp,
        lineHeight = 19.2.sp,
        letterSpacing = 0.sp,
    )

    val Body = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal, // 400
        fontSize = 15.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
    )

    val Caption = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium, // 500
        fontSize = 12.sp,
        lineHeight = 14.4.sp,
        letterSpacing = 0.sp,
    )

    val Btn = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 16.sp,
        lineHeight = 19.2.sp,
        letterSpacing = 0.sp,
    )

    val SubBtn = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Bold, // 700
        fontSize = 14.sp,
        lineHeight = 16.8.sp,
        letterSpacing = 0.sp,
    )

    val Number = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 14.sp,
        lineHeight = 20.3.sp,
        letterSpacing = 0.14.sp,
    )

    val ToastBody = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal, // 400
        fontSize = 14.sp,
        lineHeight = 20.3.sp,
        letterSpacing = 0.sp,
    )

    val SubNumber = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 13.sp,
        lineHeight = 18.85.sp,
        letterSpacing = 0.13.sp,
    )

    val SplashName = TextStyle(
        fontFamily = Montserrat,
        fontWeight = FontWeight.SemiBold, // 600
        fontSize = 38.sp,
        lineHeight = 45.6.sp,
        letterSpacing = 0.38.sp,
    )
}

val ChacTypography = Typography(
    headlineLarge = ChacTextStyles.Headline01,
    headlineMedium = ChacTextStyles.Headline02,
    titleLarge = ChacTextStyles.Title,
    titleMedium = ChacTextStyles.SubTitle01,
    titleSmall = ChacTextStyles.SubTitle02,
    bodyLarge = ChacTextStyles.Body,
    bodyMedium = ChacTextStyles.ToastBody,
    labelLarge = ChacTextStyles.Btn,
    labelMedium = ChacTextStyles.SubBtn,
    labelSmall = ChacTextStyles.Caption,
)
