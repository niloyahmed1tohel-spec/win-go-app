package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WinGoColorScheme = darkColorScheme(
    primary = WinGoGreen,
    onPrimary = Color(0xFF00381B),
    primaryContainer = WinGoGreenContainer,
    onPrimaryContainer = WinGoGreen,

    secondary = WinGoViolet,
    onSecondary = Color.White,
    secondaryContainer = WinGoVioletContainer,
    onSecondaryContainer = Color(0xFFE0AAFF),

    tertiary = WinGoGold,
    onTertiary = Color(0xFF332000),
    tertiaryContainer = Color(0xFF4A3400),
    onTertiaryContainer = WinGoGold,

    error = WinGoRed,
    onError = Color.White,
    errorContainer = WinGoRedContainer,
    onErrorContainer = WinGoRed,

    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WinGoColorScheme,
        typography = Typography,
        content = content
    )
}
