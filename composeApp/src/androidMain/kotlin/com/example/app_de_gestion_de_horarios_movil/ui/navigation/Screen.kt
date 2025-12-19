package com.example.app_de_gestion_de_horarios_movil.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

// CAMBIO 1: 'icon' ahora es nullable (ImageVector?) y tiene valor por defecto null
sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {

    // Pantallas principales (SÍ tienen icono)
    object Calendario : Screen("calendario", "Calendario", Icons.Default.DateRange)
    object Tareas : Screen("tareas", "Tareas", Icons.Default.CheckCircle)
    object Horarios : Screen("horarios", "Horarios", Icons.Default.EditCalendar)
    object Ajustes : Screen("ajustes", "Ajustes", Icons.Default.Settings)

    // Sub-pantallas (NO tienen icono, ahora esto es válido)
    object AjustesTema : Screen("ajustes/tema", "Apariencia")
}