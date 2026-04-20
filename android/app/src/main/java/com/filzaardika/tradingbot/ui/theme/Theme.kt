package com.filzaardika.tradingbot.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = AccentGreen,
    onPrimary = BgBase,
    secondary = AccentBlue,
    onSecondary = BgBase,
    tertiary = AccentPurple,
    background = BgBase,
    onBackground = TextPrimary,
    surface = BgElevated,
    onSurface = TextPrimary,
    surfaceVariant = BgCard,
    onSurfaceVariant = TextSecondary,
    error = AccentRed,
    onError = BgBase,
    outline = Border
)

@Composable
fun TradingBotTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AppTypography,
        content = content
    )
}
