package com.example.app_de_gestion_de_horarios_movil

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.app_de_gestion_de_horarios_movil.ui.navigation.AppNavigation
import com.example.app_de_gestion_de_horarios_movil.ui.theme.AppDeGestionDeHorariosMovilTheme
import com.example.app_de_gestion_de_horarios_movil.ui.features.settings.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {

    // Registramos el "contrato" para pedir permisos
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // El usuario aceptó. ¡Genial!
        } else {
            // El usuario rechazó. No podremos mostrar alertas visuales.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. VERIFICAR Y PEDIR PERMISO DE NOTIFICACIONES (ANDROID 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Si no tenemos permiso, lo pedimos ahora mismo al abrir la app
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Inyección de Koin para los temas
        val settingsViewModel: SettingsViewModel by viewModel()

        setContent {
            val settings by settingsViewModel.settingsState.collectAsState()

            AppDeGestionDeHorariosMovilTheme(
                themeMode = settings.themeMode,
                colorPalette = settings.colorPalette
            ) {
                AppNavigation()
            }
        }
    }
}