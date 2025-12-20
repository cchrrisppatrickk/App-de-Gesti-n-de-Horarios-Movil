package com.example.app_de_gestion_de_horarios_movil.ui.features.settings.subscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.app_de_gestion_de_horarios_movil.domain.model.AppColorPalette
import com.example.app_de_gestion_de_horarios_movil.domain.model.AppThemeMode
import com.example.app_de_gestion_de_horarios_movil.ui.features.settings.SettingsViewModel
import com.example.app_de_gestion_de_horarios_movil.ui.features.settings.components.SettingsSectionHeader
import com.example.app_de_gestion_de_horarios_movil.ui.theme.PrimaryBlue
import com.example.app_de_gestion_de_horarios_movil.ui.theme.PrimaryCoral
import com.example.app_de_gestion_de_horarios_movil.ui.theme.PrimaryCyan
import com.example.app_de_gestion_de_horarios_movil.ui.theme.PrimaryEmerald
import com.example.app_de_gestion_de_horarios_movil.ui.theme.PrimaryLime
import com.example.app_de_gestion_de_horarios_movil.ui.theme.PrimaryOrange
import com.example.app_de_gestion_de_horarios_movil.ui.theme.PrimaryPurple
import com.example.app_de_gestion_de_horarios_movil.ui.theme.PrimaryWhite
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel() // Inyección automática
) {
    // Observamos el estado real del repositorio
    val settings by viewModel.settingsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apariencia") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
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
        ) {

            // --- SECCIÓN 1: MODO (Claro/Oscuro/Sistema) ---
            SettingsSectionHeader("MODO")

            ThemeOptionItem(
                text = "Sistema (Automático)",
                selected = settings.themeMode == AppThemeMode.SYSTEM,
                onClick = { viewModel.updateTheme(AppThemeMode.SYSTEM) }
            )
            ThemeOptionItem(
                text = "Claro",
                selected = settings.themeMode == AppThemeMode.LIGHT,
                onClick = { viewModel.updateTheme(AppThemeMode.LIGHT) }
            )
            ThemeOptionItem(
                text = "Oscuro",
                selected = settings.themeMode == AppThemeMode.DARK,
                onClick = { viewModel.updateTheme(AppThemeMode.DARK) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // --- SECCIÓN 2: PALETA DE COLOR (Acento) ---
            SettingsSectionHeader("COLOR DE ACENTO")

            // Fila 1: Cálidos
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(PrimaryCoral, "Coral", settings.colorPalette == AppColorPalette.CORAL) {
                    viewModel.updatePalette(AppColorPalette.CORAL)
                }
                ColorPaletteItem(PrimaryOrange, "Naranja", settings.colorPalette == AppColorPalette.ORANGE) {
                    viewModel.updatePalette(AppColorPalette.ORANGE)
                }
                ColorPaletteItem(PrimaryLime, "Lima", settings.colorPalette == AppColorPalette.LIME) {
                    viewModel.updatePalette(AppColorPalette.LIME)
                }
                ColorPaletteItem(PrimaryWhite, "Mono", settings.colorPalette == AppColorPalette.MONOCHROME) {
                    viewModel.updatePalette(AppColorPalette.MONOCHROME)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fila 2: Fríos
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(PrimaryBlue, "Azul", settings.colorPalette == AppColorPalette.BLUE) {
                    viewModel.updatePalette(AppColorPalette.BLUE)
                }
                ColorPaletteItem(PrimaryCyan, "Celeste", settings.colorPalette == AppColorPalette.CYAN) {
                    viewModel.updatePalette(AppColorPalette.CYAN)
                }
                ColorPaletteItem(PrimaryPurple, "Púrpura", settings.colorPalette == AppColorPalette.PURPLE) {
                    viewModel.updatePalette(AppColorPalette.PURPLE)
                }
                ColorPaletteItem(PrimaryEmerald, "Verde", settings.colorPalette == AppColorPalette.EMERALD) {
                    viewModel.updatePalette(AppColorPalette.EMERALD)
                }
            }

            // Espacio extra al final para el scroll
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Componente auxiliar para las filas de texto (Radio Buttons visuales)
@Composable
fun ThemeOptionItem(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
    }
}

// Componente auxiliar para los círculos de colores
@Composable
fun ColorPaletteItem(color: Color, name: String, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(color)
                .clickable(onClick = onClick)
                .border(
                    width = if (selected) 3.dp else 0.dp,
                    color = MaterialTheme.colorScheme.onBackground, // Borde blanco/gris si está seleccionado
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}