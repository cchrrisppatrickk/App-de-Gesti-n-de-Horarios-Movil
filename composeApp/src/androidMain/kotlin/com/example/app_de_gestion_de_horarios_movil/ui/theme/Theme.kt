package com.example.app_de_gestion_de_horarios_movil.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.app_de_gestion_de_horarios_movil.domain.model.AppColorPalette
import com.example.app_de_gestion_de_horarios_movil.domain.model.AppThemeMode

@Composable
fun AppDeGestionDeHorariosMovilTheme(
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

    // 2. Determinar los colores principales
    val (primaryColor, secondaryColor) = when (colorPalette) {
        AppColorPalette.CORAL -> Pair(if (darkTheme) PrimaryCoral else PrimaryCoralLight, SecondaryCoral)
        AppColorPalette.ORANGE -> Pair(if (darkTheme) PrimaryOrange else PrimaryOrangeLight, SecondaryOrange)
        AppColorPalette.BLUE -> Pair(if (darkTheme) PrimaryBlue else PrimaryBlueLight, SecondaryBlue)
        AppColorPalette.PURPLE -> Pair(if (darkTheme) PrimaryPurple else PrimaryPurpleLight, SecondaryPurple)
        AppColorPalette.LIME -> Pair(if (darkTheme) PrimaryLime else PrimaryLimeLight, SecondaryLime)
        AppColorPalette.CYAN -> Pair(if (darkTheme) PrimaryCyan else PrimaryCyanLight, SecondaryCyan)
        AppColorPalette.EMERALD -> Pair(if (darkTheme) PrimaryEmerald else PrimaryEmeraldLight, SecondaryEmerald)
        AppColorPalette.MONOCHROME -> Pair(if (darkTheme) PrimaryWhite else PrimaryBlack, SecondaryWhite)
    }

    // 3. Construir el esquema de colores
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = primaryColor,
            // CORRECCIÓN 1: Forzamos Blanco para que el texto de los botones resalte
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.3f),
            onPrimaryContainer = primaryColor,

            secondary = secondaryColor,
            onSecondary = Color.White,
            secondaryContainer = secondaryColor.copy(alpha = 0.3f),
            onSecondaryContainer = secondaryColor,

            tertiary = primaryColor,
            onTertiary = Color.White,
            tertiaryContainer = primaryColor.copy(alpha = 0.3f),
            onTertiaryContainer = primaryColor,

            background = BackgroundBlack,
            onBackground = TextGray,

            surface = SurfaceDark,
            onSurface = TextGray,

            // Variantes para evitar morados
            surfaceVariant = SurfaceDark,
            onSurfaceVariant = TextDarkGray,

            outline = TextDarkGray,
            outlineVariant = TextDarkGray.copy(alpha = 0.5f),

            error = PrimaryCoral,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.2f),
            onPrimaryContainer = primaryColor,

            secondary = secondaryColor,
            onSecondary = Color.White,

            tertiary = primaryColor,

            background = BackgroundWhite,
            onBackground = TextBlack,

            surface = SurfaceWhite,
            onSurface = TextBlack,

            surfaceVariant = Color(0xFFF0F0F0),
            onSurfaceVariant = TextLightGray,

            outline = TextLightGray,
            outlineVariant = TextLightGray.copy(alpha = 0.5f)
        )
    }

    // 4. Configuración de Barras del Sistema (Status Bar y Navigation Bar)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)

            // A. Barra de Estado (Arriba)
            window.statusBarColor = if (darkTheme) ToolbarDark.toArgb() else primaryColor.toArgb()
            insetsController.isAppearanceLightStatusBars = !darkTheme

            // B. CORRECCIÓN 2: Barra de Navegación (Abajo - Botones Android)
            // La pintamos del color SurfaceDark (#252525) para que se fusione con tu menú inferior
            window.navigationBarColor = if (darkTheme) SurfaceDark.toArgb() else SurfaceWhite.toArgb()

            // Esto controla si los iconos (triángulo, círculo, cuadrado) son oscuros o claros
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}