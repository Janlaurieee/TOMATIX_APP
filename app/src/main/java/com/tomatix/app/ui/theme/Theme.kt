package com.tomatix.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Green600,
    onPrimary = White,
    primaryContainer = Green100,
    onPrimaryContainer = Green900,
    secondary = Blue500,
    onSecondary = White,
    secondaryContainer = Blue100,
    onSecondaryContainer = Blue600,
    tertiary = Orange500,
    onTertiary = White,
    tertiaryContainer = Orange100,
    onTertiaryContainer = Orange600,
    error = Red600,
    onError = White,
    errorContainer = Red100,
    onErrorContainer = Red600,
    background = Background,
    onBackground = Gray800,
    surface = White,
    onSurface = Gray800,
    surfaceVariant = Gray100,
    onSurfaceVariant = Gray600,
    outline = Gray200,
    outlineVariant = Gray300,
)

@Composable
fun TomatixTheme(content: @Composable () -> Unit) {
    val colorScheme = LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = White.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
