package com.spgg.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Lagoon,
    onPrimary = Color.White,
    primaryContainer = LagoonMist,
    onPrimaryContainer = LagoonDark,
    secondary = TideTeal,
    onSecondary = Color.White,
    secondaryContainer = TideTealSoft,
    onSecondaryContainer = Color(0xFF075A6E),
    tertiary = CoralPulse,
    onTertiary = Color.White,
    tertiaryContainer = CoralSoft,
    onTertiaryContainer = Color(0xFF7A2E10),
    background = Mist,
    onBackground = Ink,
    surface = Cloud,
    onSurface = Ink,
    surfaceVariant = LagoonFoam,
    onSurfaceVariant = InkSoft,
    surfaceContainerLowest = Cloud,
    surfaceContainerLow = Color(0xFFFAFBFE),
    surfaceContainer = Color(0xFFF3F6FB),
    surfaceContainerHigh = Color(0xFFEDF1F8),
    surfaceContainerHighest = Color(0xFFE6EBF3),
    outline = Ripple,
    outlineVariant = Color(0xFFE4E9F1),
    error = DangerRed,
    onError = Color.White,
    errorContainer = DangerSoft,
    onErrorContainer = Color(0xFF7F1D1D)
)

/** Material Design 3 brand theme. The app is intentionally light-only: it is
 *  used outdoors by delivery crews and customers, where a bright UI reads best. */
@Composable
fun SaafPaniGharGharTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = SpggTypography,
        shapes = SpggShapes,
        content = content
    )
}
