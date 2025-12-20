package com.example.app_de_gestion_de_horarios_movil.ui.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_de_gestion_de_horarios_movil.domain.model.AppColorPalette
import com.example.app_de_gestion_de_horarios_movil.domain.model.AppThemeMode
import com.example.app_de_gestion_de_horarios_movil.domain.model.UserSettings
import com.example.app_de_gestion_de_horarios_movil.domain.repository.IUserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: IUserPreferencesRepository
) : ViewModel() {

    // Convertimos el Flow del repositorio en un StateFlow para Compose.
    // "SharingStarted.WhileSubscribed(5000)" mantiene los datos vivos 5 segs al rotar pantalla.
    val settingsState: StateFlow<UserSettings> = repository.userSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings() // Valores por defecto
        )

    // Funciones para que la UI cambie los datos
    fun updateTheme(mode: AppThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun updatePalette(palette: AppColorPalette) {
        viewModelScope.launch {
            repository.setColorPalette(palette)
        }
    }


    //NOTIFICVAVINES
    fun toggleMasterNotifications(enabled: Boolean) {
        viewModelScope.launch { repository.setNotificationsEnabled(enabled) }
    }

    fun toggleNotifyAtStart(enabled: Boolean) {
        viewModelScope.launch { repository.updateNotifyAtStart(enabled) }
    }

    fun toggleNotifyAtEnd(enabled: Boolean) {
        viewModelScope.launch { repository.updateNotifyAtEnd(enabled) }
    }

    fun toggleNotify15Min(enabled: Boolean) {
        viewModelScope.launch { repository.updateNotify15MinutesBefore(enabled) }
    }
}