package com.spgg.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Clean-water blue — matches the React admin portal's theme.
val WaterBlue = Color(0xFF0288D1)

private val LightColors = lightColorScheme(primary = WaterBlue)
private val DarkColors = darkColorScheme(primary = WaterBlue)

/** Material Design 3 per NFR-UX-001, with light/dark following system preference (NFR-UX-003). */
@Composable
fun SaafPaniGharGharTheme(content: @Composable () -> Unit) {
    val colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colorScheme, content = content)
}
