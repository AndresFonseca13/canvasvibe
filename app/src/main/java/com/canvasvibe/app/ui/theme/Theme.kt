package com.canvasvibe.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CanvasVibeColorScheme = darkColorScheme(
    primary       = Primary,
    onPrimary     = TextPrimary,
    secondary     = PrimaryAccent,
    onSecondary   = TextPrimary,
    background    = Background,
    onBackground  = TextPrimary,
    surface       = SurfaceDark,
    onSurface     = TextPrimary,
    surfaceVariant   = SurfaceDark,
    onSurfaceVariant = TextSecondary,
    outline       = BorderSubtle,
    error         = ErrorRed,
    onError       = TextPrimary
)

@Composable
fun CanvasVibeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CanvasVibeColorScheme,
        typography  = Typography,
        content     = content
    )
}