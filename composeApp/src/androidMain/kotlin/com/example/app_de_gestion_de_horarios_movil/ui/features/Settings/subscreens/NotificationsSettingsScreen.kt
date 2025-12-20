package com.example.app_de_gestion_de_horarios_movil.ui.features.settings.subscreens

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.app_de_gestion_de_horarios_movil.ui.features.settings.SettingsViewModel
import com.example.app_de_gestion_de_horarios_movil.ui.features.settings.components.SettingsGroupCard
import com.example.app_de_gestion_de_horarios_movil.ui.features.settings.components.SettingsItem
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val settings by viewModel.settingsState.collectAsState()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp), // Edge-to-Edge
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = scrollBehavior
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

            // --- GRUPO 1: INTERRUPTOR MAESTRO ---
            SettingsGroupCard(title = "General") {
                // Personalizamos el SettingsItem para tener un Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (settings.areNotificationsEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Habilitar Notificaciones", style = MaterialTheme.typography.bodyLarge)
                        Text("Activar o desactivar todo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = settings.areNotificationsEnabled,
                        onCheckedChange = { viewModel.toggleMasterNotifications(it) }
                    )
                }
            }

            // --- GRUPO 2: PERMISOS DE SISTEMA ---
            SettingsGroupCard(title = "Sistema Android") {
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.OpenInNew,
                    title = "Sonido y Vibración",
                    subtitle = "Abrir configuración del dispositivo",
                    onClick = {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    }
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    SettingsItem(
                        icon = Icons.Default.Timer, // Icono de alarma
                        title = "Alarmas Exactas",
                        subtitle = "Permiso para precisión",
                        showDivider = false,
                        onClick = {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // --- GRUPO 3: PREFERENCIAS ---
            SettingsGroupCard(title = "Alertas por Defecto") {
                Text(
                    text = "Opciones automáticas al crear nuevas tareas",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Items con Checkbox personalizados
                CheckboxRow("Al inicio", settings.notifyAtStart) { viewModel.toggleNotifyAtStart(it) }
                CheckboxRow("15 minutos antes", settings.notify15MinutesBefore) { viewModel.toggleNotify15Min(it) }
                CheckboxRow("Al finalizar", settings.notifyAtEnd) { viewModel.toggleNotifyAtEnd(it) }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Helper local para filas con checkbox dentro de las tarjetas
@Composable
private fun CheckboxRow(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(text = text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
    }
}