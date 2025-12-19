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
    ORANGE
}

// Clase contenedora para leer toda la config de una vez
data class UserSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val colorPalette: AppColorPalette = AppColorPalette.CORAL,
    val isNotificationsEnabled: Boolean = true
)