package com.example.app_de_gestion_de_horarios_movil.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 1. ESQUEMA OSCURO (Aquí conectamos tus colores negros y rojos)
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryRed,          // <--- Tu PrimaryRed ahora es "primary"
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = SecondaryRed,
    tertiary = TextGray,
    background = BackgroundBlack,  // <--- Tu BackgroundBlack ahora es "background"
    onBackground = TextGray,
    surface = SurfaceDark,         // <--- Tu SurfaceDark ahora es "surface"
    onSurface = TextGray,
    error = PrimaryRed
)

// 2. ESQUEMA CLARO (Por si acaso)
private val LightColorScheme = lightColorScheme(
    primary = PrimaryRedLight,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    background = BackgroundWhite,
    onBackground = TextBlack,
    surface = SurfaceWhite,
    onSurface = TextBlack,
)

@Composable
fun AppDeGestionDeHorariosMovilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color lo ponemos en false para FORZAR tus colores rojos y negros
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Configuración para pintar la barra de estado (donde está la hora y batería)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Aquí pintamos la barra de estado del color de tu fondo o toolbar
            window.statusBarColor = if(darkTheme) ToolbarDark.toArgb() else PrimaryRedLight.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}