package com.mi.bibliarv1960.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccent,
    secondary = DarkAccentSoft,
    tertiary = LinoMark,
    background = DarkBg,
    surface = DarkSurface,
    onPrimary = DarkSurface,
    onSecondary = DarkInk,
    onBackground = DarkInk,
    onSurface = DarkInk,
    surfaceVariant = DarkLine,
    secondaryContainer = DarkAccentSoft,
    onSecondaryContainer = DarkAccent,
)

private val LightColorScheme = lightColorScheme(
    primary = LinoAccent,
    secondary = LinoAccentSoft,
    tertiary = LinoMark,
    background = LinoBg,
    surface = LinoSurface,
    onPrimary = LinoSurface,
    onSecondary = LinoAccent,
    onBackground = LinoInk,
    onSurface = LinoInk,
    surfaceVariant = LinoLine,
    secondaryContainer = LinoAccentSoft,
    onSecondaryContainer = LinoAccent,
)

@Composable
fun BibliaRV1960Theme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BibliaTypography,
        content = content
    )
}
