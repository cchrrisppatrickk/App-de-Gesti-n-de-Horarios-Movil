package com.example.app_de_gestion_de_horarios_movil

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.app_de_gestion_de_horarios_movil.ui.features.settings.SettingsViewModel
import com.example.app_de_gestion_de_horarios_movil.ui.navigation.AppNavigation
import com.example.app_de_gestion_de_horarios_movil.ui.theme.AppDeGestionDeHorariosMovilTheme
import org.koin.androidx.viewmodel.ext.android.viewModel // Importante para Koin

class MainActivity : ComponentActivity() {

    // Inyectamos el ViewModel de ajustes para observar los cambios globales
    private val settingsViewModel: SettingsViewModel by viewModel()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            // Observamos el estado de las preferencias
            val settings by settingsViewModel.settingsState.collectAsState()

            // Pasamos las preferencias al Tema
            AppDeGestionDeHorariosMovilTheme(
                themeMode = settings.themeMode,
                colorPalette = settings.colorPalette
            ) {
                // Llamamos a la navegación principal
                AppNavigation()
            }
        }
    }
}