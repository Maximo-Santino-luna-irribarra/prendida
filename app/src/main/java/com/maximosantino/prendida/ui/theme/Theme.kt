package com.maximosantino.prendida.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryViolet,
    secondary = SecondaryViolet,
    tertiary = SuccessGreen,
    background = BackgroundBlack,
    surface = SurfaceWhite,
    onPrimary = BackgroundBlack,
    onSecondary = BackgroundBlack,
    onTertiary = BackgroundBlack,
    onBackground = TextWhite,
    onSurface = TextBlack,
    error = ErrorRed,
    onError = TextWhite
)

// Forzamos el modo oscuro por el estilo de la app
private val LightColorScheme = DarkColorScheme

@Composable
fun PrendidaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Desactivamos dynamic color para mantener la identidad visual de la app
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme // Forzamos DarkColorScheme siempre
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
