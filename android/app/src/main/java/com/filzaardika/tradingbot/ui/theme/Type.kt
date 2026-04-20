package com.filzaardika.tradingbot.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Sans = FontFamily.Default
private val Mono = FontFamily.Monospace  // used for tabular numerics (equity, P&L, prices)

val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = Sans, fontSize = 44.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp),
    displayMedium = TextStyle(fontFamily = Sans, fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontFamily = Sans, fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.25).sp),
    headlineMedium = TextStyle(fontFamily = Sans, fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontFamily = Sans, fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontFamily = Sans, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontFamily = Sans, fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontFamily = Sans, fontSize = 15.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontFamily = Sans, fontSize = 14.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontFamily = Sans, fontSize = 12.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontFamily = Sans, fontSize = 13.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.4.sp),
    labelMedium = TextStyle(fontFamily = Sans, fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.6.sp),
    labelSmall = TextStyle(fontFamily = Sans, fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.8.sp)
)

// Reusable monospace styles for numeric content
val NumericDisplay = TextStyle(fontFamily = Mono, fontSize = 44.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
val NumericLarge = TextStyle(fontFamily = Mono, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
val NumericMedium = TextStyle(fontFamily = Mono, fontSize = 16.sp, fontWeight = FontWeight.Medium)
val NumericSmall = TextStyle(fontFamily = Mono, fontSize = 13.sp, fontWeight = FontWeight.Medium)
