package com.example.app_de_gestion_de_horarios_movil.ui.features.settings.subscreens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import com.example.app_de_gestion_de_horarios_movil.domain.model.AppColorPalette
import com.example.app_de_gestion_de_horarios_movil.domain.model.AppThemeMode
import com.example.app_de_gestion_de_horarios_movil.ui.features.settings.SettingsViewModel
import com.example.app_de_gestion_de_horarios_movil.ui.features.settings.components.SettingsGroupCard
import com.example.app_de_gestion_de_horarios_movil.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val settings by viewModel.settingsState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp), // Edge-to-Edge
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
        ) {

            // --- GRUPO 1: MODO ---
            SettingsGroupCard(title = "Modo") {
                RadioButtonRow("Sistema", settings.themeMode == AppThemeMode.SYSTEM) { viewModel.updateTheme(AppThemeMode.SYSTEM) }
                RadioButtonRow("Claro", settings.themeMode == AppThemeMode.LIGHT) { viewModel.updateTheme(AppThemeMode.LIGHT) }
                RadioButtonRow("Oscuro", settings.themeMode == AppThemeMode.DARK) { viewModel.updateTheme(AppThemeMode.DARK) }
            }

            // --- GRUPO 2: PALETA DE COLORES ---
            SettingsGroupCard(title = "Acento") {
                // Fila 1: Cálidos
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ColorPaletteItem(PrimaryCoral, "Coral", settings.colorPalette == AppColorPalette.CORAL) { viewModel.updatePalette(AppColorPalette.CORAL) }
                    ColorPaletteItem(PrimaryOrange, "Naranja", settings.colorPalette == AppColorPalette.ORANGE) { viewModel.updatePalette(AppColorPalette.ORANGE) }
                    ColorPaletteItem(PrimaryLime, "Lima", settings.colorPalette == AppColorPalette.LIME) { viewModel.updatePalette(AppColorPalette.LIME) }
                    ColorPaletteItem(PrimaryWhite, "Mono", settings.colorPalette == AppColorPalette.MONOCHROME) { viewModel.updatePalette(AppColorPalette.MONOCHROME) }
                }

                // Fila 2: Fríos
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ColorPaletteItem(PrimaryBlue, "Azul", settings.colorPalette == AppColorPalette.BLUE) { viewModel.updatePalette(AppColorPalette.BLUE) }
                    ColorPaletteItem(PrimaryCyan, "Celeste", settings.colorPalette == AppColorPalette.CYAN) { viewModel.updatePalette(AppColorPalette.CYAN) }
                    ColorPaletteItem(PrimaryPurple, "Púrpura", settings.colorPalette == AppColorPalette.PURPLE) { viewModel.updatePalette(AppColorPalette.PURPLE) }
                    ColorPaletteItem(PrimaryEmerald, "Verde", settings.colorPalette == AppColorPalette.EMERALD) { viewModel.updatePalette(AppColorPalette.EMERALD) }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// Helpers Locales para UI limpia
@Composable
private fun RadioButtonRow(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 16.dp))
    }
}

@Composable
private fun ColorPaletteItem(color: Color, name: String, selected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(50.dp) // Un poco más pequeños para caber en la tarjeta
                .clip(CircleShape)
                .background(color)
                .clickable(onClick = onClick)
                .border(
                    width = if (selected) 3.dp else 0.dp,
                    color = MaterialTheme.colorScheme.onSurface,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = if(name == "Mono") Color.Black else Color.White, modifier = Modifier.size(20.dp))
            }
        }
        Text(text = name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
    }
}