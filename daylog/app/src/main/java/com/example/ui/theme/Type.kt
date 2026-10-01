package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// ==========================================
// Aurora Glass Typography (Sora)
// ==========================================

val SoraFontFamily = FontFamily(
    Font(R.font.sora, FontWeight.Normal),
    Font(R.font.sora, FontWeight.Medium),
    Font(R.font.sora, FontWeight.SemiBold),
    Font(R.font.sora, FontWeight.Bold)
)

object DaylogTypography {
    val Display = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        color = DaylogTokens.TextPrimary
    )

    val Numeral = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        fontFeatureSettings = "tnum",
        color = DaylogTokens.TextPrimary
    )

    val Title = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        color = DaylogTokens.TextPrimary
    )

    val Body = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = DaylogTokens.TextSecondary
    )

    val Label = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
        color = DaylogTokens.TextSecondary
    )

    val Caption = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        color = DaylogTokens.TextTertiary
    )
}

val Typography = Typography(
    displayLarge = DaylogTypography.Display,
    displayMedium = DaylogTypography.Display.copy(fontSize = 28.sp, lineHeight = 34.sp),
    headlineLarge = DaylogTypography.Numeral,
    headlineMedium = DaylogTypography.Numeral.copy(fontSize = 32.sp, lineHeight = 38.sp),
    headlineSmall = DaylogTypography.Title.copy(fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = DaylogTypography.Title,
    titleMedium = DaylogTypography.Title.copy(fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = DaylogTypography.Title.copy(fontSize = 14.sp, lineHeight = 18.sp),
    bodyLarge = DaylogTypography.Body.copy(color = DaylogTokens.TextPrimary),
    bodyMedium = DaylogTypography.Body,
    bodySmall = DaylogTypography.Body.copy(fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = DaylogTypography.Label.copy(fontSize = 13.sp),
    labelMedium = DaylogTypography.Label,
    labelSmall = DaylogTypography.Caption
)
