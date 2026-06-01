package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = TurfGreenPrimary,
    secondary = SportsNeonAccent,
    tertiary = SportsOrangeAccent,
    background = PitchDarkBackground,
    surface = PitchDarkCard,
    onPrimary = PitchDarkBackground,
    onSecondary = PitchDarkBackground,
    onTertiary = PitchDarkBackground,
    onBackground = TextLightHigh,
    onSurface = TextLightHigh,
    outline = PitchBorder,
    surfaceVariant = PitchDarkCard,
    onSurfaceVariant = TextLightMed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force gorgeous tactical dark mode
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
