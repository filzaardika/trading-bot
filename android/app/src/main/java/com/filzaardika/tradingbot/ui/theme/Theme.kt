package com.filzaardika.tradingbot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = AccentGreen,
    onPrimary = BgBase,
    primaryContainer = AccentGreenDeep,
    onPrimaryContainer = BgBase,
    secondary = AccentBlue,
    onSecondary = BgBase,
    tertiary = AccentPurple,
    onTertiary = BgBase,
    background = BgBase,
    onBackground = TextPrimary,
    surface = BgElevated,
    onSurface = TextPrimary,
    surfaceVariant = BgCard,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = BgBase,
    surfaceContainerLow = BgElevated,
    surfaceContainer = BgCard,
    surfaceContainerHigh = BgCardHigh,
    surfaceContainerHighest = BgCardHigh,
    error = AccentRedDeep,
    onError = TextPrimary,
    errorContainer = KillGradBottom,
    onErrorContainer = TextPrimary,
    outline = Border,
    outlineVariant = BorderStrong
)

@Composable
fun TradingBotTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AppTypography,
        content = content
    )
}
