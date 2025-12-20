package com.example.app_de_gestion_de_horarios_movil.ui.features.settings.subscreens

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.app_de_gestion_de_horarios_movil.ui.features.settings.SettingsViewModel
import com.example.app_de_gestion_de_horarios_movil.ui.features.settings.components.SettingsSectionHeader
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val settings by viewModel.settingsState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
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

            // 1. MASTER SWITCH
            // Desactiva toda la lógica de notificaciones de la app
            SwitchSettingItem(
                title = "Habilitar Notificaciones",
                subtitle = "Activar o desactivar todas las alertas",
                checked = settings.areNotificationsEnabled,
                icon = if (settings.areNotificationsEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                onCheckedChange = { viewModel.toggleMasterNotifications(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 2. CONFIGURACIÓN DEL SISTEMA (Responde a tu duda A)
            // Llevamos al usuario a Android Settings para controlar Sonido, Vibración, Bloqueo
            SettingsSectionHeader("PERMISOS DEL DISPOSITIVO")

            SystemSettingsLinkItem(
                text = "Configurar Sonido y Vibración",
                onClick = {
                    // Intent para abrir detalles de la app en Ajustes de Android
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    context.startActivity(intent)
                }
            )

            // Botón para Alarmas Exactas (Responde a tu duda B)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SystemSettingsLinkItem(
                    text = "Permiso de Alarmas Exactas",
                    subtitle = "Necesario para alertas precisas",
                    onClick = {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        context.startActivity(intent)
                    }
                )
            }

            // 3. DEFAULTS DE NEGOCIO (Responde a tu sección de Alertas)
            // Esto controla qué checkboxes aparecen marcados al crear una nueva tarea
            SettingsSectionHeader("ALERTAS PREDETERMINADAS")
            Text(
                text = "Estas opciones se aplicarán automáticamente al crear nuevas tareas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            CheckboxSettingItem(
                text = "Al inicio de la tarea",
                checked = settings.notifyAtStart,
                onCheckedChange = { viewModel.toggleNotifyAtStart(it) }
            )

            CheckboxSettingItem(
                text = "15 minutos antes",
                checked = settings.notify15MinutesBefore,
                onCheckedChange = { viewModel.toggleNotify15Min(it) }
            )

            CheckboxSettingItem(
                text = "Al finalizar la tarea",
                checked = settings.notifyAtEnd,
                onCheckedChange = { viewModel.toggleNotifyAtEnd(it) }
            )

            // Espacio final
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- COMPONENTES AUXILIARES LOCALES ---

@Composable
fun SwitchSettingItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SystemSettingsLinkItem(
    text: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = text, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = "Abrir",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun CheckboxSettingItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}