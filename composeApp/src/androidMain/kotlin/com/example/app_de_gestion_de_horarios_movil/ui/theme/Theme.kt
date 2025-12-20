package com.example.app_de_gestion_de_horarios_movil.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.app_de_gestion_de_horarios_movil.domain.model.AppColorPalette
import com.example.app_de_gestion_de_horarios_movil.domain.model.AppThemeMode

@Composable
fun AppDeGestionDeHorariosMovilTheme(
    // Parámetros nuevos para controlar el tema desde fuera
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    colorPalette: AppColorPalette = AppColorPalette.CORAL,
    content: @Composable () -> Unit
) {
    // 1. Determinar si es Oscuro o Claro
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    // 2. Determinar los colores principales según la paleta elegida
    val (primaryColor, secondaryColor) = when (colorPalette) {
        AppColorPalette.CORAL -> Pair(
            if (darkTheme) PrimaryCoral else PrimaryCoralLight,
            SecondaryCoral
        )
        AppColorPalette.ORANGE -> Pair(
            if (darkTheme) PrimaryOrange else PrimaryOrangeLight,
            SecondaryOrange
        )
        AppColorPalette.BLUE -> Pair(
            if (darkTheme) PrimaryBlue else PrimaryBlueLight,
            SecondaryBlue
        )
        AppColorPalette.PURPLE -> Pair(
            if (darkTheme) PrimaryPurple else PrimaryPurpleLight,
            SecondaryPurple
        )
        AppColorPalette.LIME -> Pair(
            if (darkTheme) PrimaryLime else PrimaryLimeLight,
            SecondaryLime
        )
        AppColorPalette.CYAN -> Pair(
            if (darkTheme) PrimaryCyan else PrimaryCyanLight,
            SecondaryCyan
        )
        AppColorPalette.EMERALD -> Pair(
            if (darkTheme) PrimaryEmerald else PrimaryEmeraldLight,
            SecondaryEmerald
        )
        AppColorPalette.MONOCHROME -> Pair(
            if (darkTheme) PrimaryWhite else PrimaryBlack, // Blanco en oscuro, Negro en claro
            SecondaryWhite
        )
    }

    // 3. Construir el esquema de colores
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = primaryColor,
            secondary = secondaryColor,
            background = BackgroundBlack,
            surface = SurfaceDark,
            onBackground = TextGray,
            onSurface = TextGray,
            // ... otros colores oscuros
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            secondary = secondaryColor,
            background = BackgroundWhite,
            surface = SurfaceWhite,
            onBackground = TextBlack,
            onSurface = TextBlack,
            // ... otros colores claros
        )
    }

    // 4. Configuración de Barra de Estado
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (darkTheme) ToolbarDark.toArgb() else primaryColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Asegúrate de tener Typography definido o usa el default
        content = content
    )
}