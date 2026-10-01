package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AuroraColorScheme = darkColorScheme(
    primary = DaylogTokens.AuroraViolet,
    onPrimary = DaylogTokens.TextPrimary,
    primaryContainer = DaylogTokens.NightDeep,
    onPrimaryContainer = DaylogTokens.TextPrimary,
    secondary = DaylogTokens.AuroraCyan,
    onSecondary = DaylogTokens.Night,
    secondaryContainer = DaylogTokens.NightDeep,
    onSecondaryContainer = DaylogTokens.TextSecondary,
    tertiary = DaylogTokens.AuroraMagenta,
    onTertiary = DaylogTokens.Night,
    tertiaryContainer = DaylogTokens.NightDeep,
    onTertiaryContainer = DaylogTokens.TextPrimary,
    error = DaylogTokens.Distraction,
    onError = Color.White,
    errorContainer = DaylogTokens.Distraction.copy(alpha = 0.15f),
    onErrorContainer = DaylogTokens.Distraction,
    background = DaylogTokens.Night,
    onBackground = DaylogTokens.TextPrimary,
    surface = DaylogTokens.NightDeep,
    onSurface = DaylogTokens.TextPrimary,
    surfaceVariant = DaylogTokens.NightDeep,
    onSurfaceVariant = DaylogTokens.TextSecondary,
    outline = DaylogTokens.GlassHighlightTop,
    outlineVariant = DaylogTokens.TextTertiary.copy(alpha = 0.3f)
)

@Composable
fun DaylogTheme(
    darkTheme: Boolean = true, // Dark only as per Section 7: "Theme. Dark only. Do not add a light theme."
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AuroraColorScheme,
        typography = Typography,
        content = content
    )
}
