package com.project.roulette.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.project.roulette.R

val AppFontFamily = FontFamily(
    Font(R.font.harmonyos_sans_regular, FontWeight.Normal),
    Font(R.font.harmonyos_sans_medium,  FontWeight.Medium),
    Font(R.font.harmonyos_sans_bold,    FontWeight.Bold)
)

private fun appStyle(
    weight: FontWeight,
    size: Int,
    lineHeight: Int,
    letterSpacing: Double = 0.0
) = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp
)

val Typography = Typography(
    displayLarge = appStyle(FontWeight.Bold, 40, 48, -0.5),
    displayMedium = appStyle(FontWeight.Bold, 34, 42, -0.4),
    displaySmall = appStyle(FontWeight.Bold, 28, 36, -0.3),

    headlineLarge = appStyle(FontWeight.Bold, 28, 36, -0.3),
    headlineMedium = appStyle(FontWeight.Bold, 24, 32, -0.2),
    headlineSmall = appStyle(FontWeight.Bold, 22, 28, -0.2),

    titleLarge = appStyle(FontWeight.Bold, 20, 26, -0.1),
    titleMedium = appStyle(FontWeight.Medium, 17, 22),
    titleSmall = appStyle(FontWeight.Medium, 15, 20),

    bodyLarge = appStyle(FontWeight.Normal, 17, 22),
    bodyMedium = appStyle(FontWeight.Normal, 16, 21),
    bodySmall = appStyle(FontWeight.Normal, 14, 19),

    labelLarge = appStyle(FontWeight.Medium, 15, 20),
    labelMedium = appStyle(FontWeight.Medium, 12, 16, 0.6),
    labelSmall = appStyle(FontWeight.Medium, 11, 14, 0.4)
)
