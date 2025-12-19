package com.example.app_de_gestion_de_horarios_movil.domain.repository

import com.example.app_de_gestion_de_horarios_movil.domain.model.AppColorPalette
import com.example.app_de_gestion_de_horarios_movil.domain.model.AppThemeMode
import com.example.app_de_gestion_de_horarios_movil.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface IUserPreferencesRepository {
    // Observar todos los ajustes (para que la UI se redibuje sola)
    val userSettings: Flow<UserSettings>

    // Métodos para guardar cambios individuales
    suspend fun setThemeMode(mode: AppThemeMode)
    suspend fun setColorPalette(palette: AppColorPalette)
    suspend fun setNotificationsEnabled(enabled: Boolean)
}