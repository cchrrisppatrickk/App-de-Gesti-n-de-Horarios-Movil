package com.example.app_de_gestion_de_horarios_movil.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.app_de_gestion_de_horarios_movil.domain.model.AppColorPalette
import com.example.app_de_gestion_de_horarios_movil.domain.model.AppThemeMode
import com.example.app_de_gestion_de_horarios_movil.domain.model.UserSettings
import com.example.app_de_gestion_de_horarios_movil.domain.repository.IUserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión para crear el DataStore (Singleton)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferencesRepositoryImpl(private val context: Context) : IUserPreferencesRepository {

    // Definición de Claves
    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val COLOR_PALETTE = stringPreferencesKey("color_palette")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")

        val NOTIFY_AT_START = booleanPreferencesKey("notify_start")
        val NOTIFY_AT_END = booleanPreferencesKey("notify_end")
        val NOTIFY_15_MIN = booleanPreferencesKey("notify_15min")
        val MASTER_NOTIFICATIONS = booleanPreferencesKey("master_notifications")
    }

    // Flujo principal: Lee de disco y convierte a objetos de Dominio
    override val userSettings: Flow<UserSettings> = context.dataStore.data
        .map { preferences ->
            // 1. Leer Theme Mode (Default: SYSTEM)
            val themeString = preferences[PreferencesKeys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
            val themeMode = try {
                AppThemeMode.valueOf(themeString)
            } catch (e: Exception) { AppThemeMode.SYSTEM }

            // 2. Leer Color Palette (Default: CORAL)
            val paletteString = preferences[PreferencesKeys.COLOR_PALETTE] ?: AppColorPalette.CORAL.name
            val colorPalette = try {
                AppColorPalette.valueOf(paletteString)
            } catch (e: Exception) { AppColorPalette.CORAL }

            // 3. Leer Notificaciones (Default: true)
            val notifications = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true

            UserSettings(
                themeMode = themeMode,
                colorPalette = colorPalette,
                // Lectura de nuevas propiedades
                notifyAtStart = preferences[PreferencesKeys.NOTIFY_AT_START] ?: true,
                notifyAtEnd = preferences[PreferencesKeys.NOTIFY_AT_END] ?: false,
                notify15MinutesBefore = preferences[PreferencesKeys.NOTIFY_15_MIN] ?: true,
                areNotificationsEnabled = preferences[PreferencesKeys.MASTER_NOTIFICATIONS] ?: true
            )
        }

    // --- Métodos de Guardado ---

    override suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    override suspend fun setColorPalette(palette: AppColorPalette) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COLOR_PALETTE] = palette.name
        }
    }

    override suspend fun updateNotifyAtStart(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.NOTIFY_AT_START] = enabled }
    }


    override suspend fun updateNotifyAtEnd(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.NOTIFY_AT_END] = enabled }
    }

    override suspend fun updateNotify15MinutesBefore(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.NOTIFY_15_MIN] = enabled }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.MASTER_NOTIFICATIONS] = enabled }
    }








}