package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val SilkscreenFamily = FontFamily(Font(R.font.silkscreen, FontWeight.Normal))
val SpaceGroteskFamily = FontFamily(Font(R.font.space_grotesk, FontWeight.Normal))
val JetBrainsMonoFamily = FontFamily(Font(R.font.jetbrains_mono, FontWeight.Normal))

val VoltDashTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = SilkscreenFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.5.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = SilkscreenFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.5.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = SilkscreenFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.4.sp
    ),
    titleLarge = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = SilkscreenFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    labelLarge = TextStyle(
        fontFamily = SilkscreenFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.6.sp
    ),
    labelMedium = TextStyle(
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.3.sp
    ),
    labelSmall = TextStyle(
        fontFamily = JetBrainsMonoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 14.sp
    )
)
