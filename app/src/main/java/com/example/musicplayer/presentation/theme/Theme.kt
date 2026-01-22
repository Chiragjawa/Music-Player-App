package com.example.musicplayer.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryOrange,
    onPrimary = Color.White,
    primaryContainer = PrimaryOrangeDark,
    onPrimaryContainer = Color.White,

    secondary = PrimaryOrangeLight,
    onSecondary = Color.White,

    background = DarkBackground,
    onBackground = TextPrimaryDark,

    surface = DarkSurface,
    onSurface = TextPrimaryDark,

    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,

    error = AccentRed,
    onError = Color.White,

    outline = TextTertiaryDark,
    outlineVariant = Color(0xFF404040)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryOrange,
    onPrimary = Color.White,
    primaryContainer = PrimaryOrangeLight,
    onPrimaryContainer = Color.White,

    secondary = PrimaryOrangeDark,
    onSecondary = Color.White,

    background = LightBackground,
    onBackground = TextPrimaryLight,

    surface = LightSurface,
    onSurface = TextPrimaryLight,

    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,

    error = AccentRed,
    onError = Color.White,

    outline = TextTertiaryLight,
    outlineVariant = Color(0xFFE0E0E0)
)

@Composable
fun MusicPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}