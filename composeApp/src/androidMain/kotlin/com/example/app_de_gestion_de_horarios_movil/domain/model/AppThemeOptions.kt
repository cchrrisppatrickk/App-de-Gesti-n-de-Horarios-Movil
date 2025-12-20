package com.example.app_de_gestion_de_horarios_movil.domain.model

// Opción A: El modo de fondo (Sistema, Claro, Oscuro)
enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

// Opción B: El color principal de la app (Coral, Naranja)
enum class AppColorPalette {
    CORAL,
    ORANGE,
    BLUE,
    PURPLE,
    LIME,
    CYAN,
    EMERALD,
    MONOCHROME
}

// Clase contenedora para leer toda la config de una vez
data class UserSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val colorPalette: AppColorPalette = AppColorPalette.CORAL,
    // Nuevas configuraciones de alertas por defecto
    val notifyAtStart: Boolean = true,
    val notifyAtEnd: Boolean = false,
    val notify15MinutesBefore: Boolean = true,
    val areNotificationsEnabled: Boolean = true // Master switch
)