package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = DeepBlue,
    onPrimaryContainer = Color.White,
    secondary = SecondaryText,
    onSecondary = Color.White,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkBorder,
    outline = DarkBorder,
    error = DangerRed
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = LightBlue,
    onPrimaryContainer = DeepBlue,
    secondary = SecondaryText,
    onSecondary = Color.White,
    secondaryContainer = LightBlue,
    onSecondaryContainer = PrimaryBlue,
    background = BackgroundLight,
    onBackground = HeadingDark,
    surface = SurfaceWhite,
    onSurface = HeadingDark,
    surfaceVariant = BackgroundLight,
    outline = BorderLight,
    error = DangerRed
)

@Composable
fun TakaTrackTheme(
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

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    TakaTrackTheme(darkTheme = darkTheme, content = content)
}
