package com.example.app_de_gestion_de_horarios_movil.ui.features.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_de_gestion_de_horarios_movil.ui.features.settings.components.SettingsGroupCard
import com.example.app_de_gestion_de_horarios_movil.ui.features.settings.components.SettingsItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToTheme: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    Scaffold(


        // 1. ELIMINAR LOS ESPACIOS DEL SISTEMA (Edge-to-Edge puro)
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),


        containerColor = MaterialTheme.colorScheme.background // Fondo negro/gris oscuro
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding) // Respetamos el padding del Scaffold
                .verticalScroll(rememberScrollState())
        ) {

            // 2. CABECERA PERSONALIZADA Y FIJA
            // En lugar de TopAppBar, usamos un Text simple con padding de status bar
            // para que quede fijo arriba y se vea limpio sobre el fondo negro.
            Column(
                modifier = Modifier
                    // IMPORTANTE: Esto baja el texto para que no quede detrás de la hora/batería
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "Ajustes",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 32.sp // Un poco más grande para impacto visual
                )
            }

            // 3. CONTENIDO (Grupos de Tarjetas)
            // Ya no hay cartilla gris envolvente, las tarjetas flotan sobre el fondo negro.

            // --- GRUPO 1: GENERAL ---
            SettingsGroupCard(title = "General") {
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notificaciones",
                    subtitle = "Alertas y recordatorios",
                    iconColor = Color(0xFFFF9F43), // Naranja
                    onClick = onNavigateToNotifications
                )

                SettingsItem(
                    icon = Icons.Default.Brightness4,
                    title = "Apariencia",
                    subtitle = "Tema y colores",
                    iconColor = MaterialTheme.colorScheme.primary, // Coral
                    showDivider = false,
                    onClick = onNavigateToTheme
                )
            }

            // --- GRUPO 2: INTELIGENCIA ---
            SettingsGroupCard(title = "Inteligencia") {
                SettingsItem(
                    icon = Icons.Default.Psychology,
                    title = "Asistente IA",
                    subtitle = "Configuración avanzada",
                    iconColor = Color(0xFF9C88FF), // Púrpura
                    showDivider = false,
                    onClick = { /* TODO */ }
                )
            }

            // --- GRUPO 3: INFORMACIÓN ---
            SettingsGroupCard(title = "Otros") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Acerca de",
                    subtitle = "v1.0.0",
                    iconColor = Color(0xFF54A0FF), // Azul
                    onClick = { /* TODO */ }
                )

                SettingsItem(
                    icon = Icons.Default.Share,
                    title = "Redes Sociales",
                    subtitle = "Síguenos",
                    iconColor = Color(0xFF2ECC71), // Verde
                    showDivider = false,
                    onClick = { /* TODO */ }
                )
            }

            // Espacio final para que el scroll no corte el último elemento
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}