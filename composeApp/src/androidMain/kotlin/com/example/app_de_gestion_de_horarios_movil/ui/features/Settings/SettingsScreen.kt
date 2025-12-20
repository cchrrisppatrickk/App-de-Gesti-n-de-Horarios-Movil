package com.example.app_de_gestion_de_horarios_movil.ui.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4 // Icono Tema
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology // Icono IA/Avanzado
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.app_de_gestion_de_horarios_movil.ui.features.settings.components.SettingsNavigationItem
import com.example.app_de_gestion_de_horarios_movil.ui.features.settings.components.SettingsSectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToTheme: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    // Aquí agregarás más callbacks conforme crees pantallas
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            // --- SECCIÓN GENERAL ---
            SettingsSectionHeader("GENERAL")

            // En la llamada a SettingsNavigationItem de Notificaciones
            SettingsNavigationItem(
                icon = Icons.Default.Notifications,
                title = "Notificaciones y Alertas",
                subtitle = "Gestionar recordatorios",
                onClick = onNavigateToNotifications // Ahora este callback funciona
            )

            SettingsNavigationItem(
                icon = Icons.Default.Brightness4,
                title = "Tema y Apariencia",
                subtitle = "Oscuro, Claro, Sistema",
                onClick = onNavigateToTheme
            )

            // --- SECCIÓN AVANZADO ---
            SettingsSectionHeader("AVANZADO")

            SettingsNavigationItem(
                icon = Icons.Default.Psychology,
                title = "Asistente Inteligente",
                subtitle = "Configuración de IA (Próximamente)",
                onClick = { /* TODO: Navegar a pantalla de IA */ }
            )

            // --- SECCIÓN OTROS ---
            SettingsSectionHeader("INFORMACIÓN")

            SettingsNavigationItem(
                icon = Icons.Default.Info,
                title = "Acerca de",
                subtitle = "Versión 1.0.0",
                onClick = { /* TODO */ }
            )

            SettingsNavigationItem(
                icon = Icons.Default.Share,
                title = "Redes Sociales",
                onClick = { /* TODO: Abrir intent de navegador */ }
            )
        }
    }
}