package com.example.musicplayer.presentation.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = PrimaryGreen,
    background = DarkBackground,
    surface = Color(0xFF181818),
    onPrimary = Color.White
)

private val LightColors = lightColorScheme(
    primary = PrimaryGreen,
    surface = Color.White
)

@Composable
fun MusicPlayerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
