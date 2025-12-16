package com.example.app_de_gestion_de_horarios_movil.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

// Sealed class enriquecida para manejar la UI de la barra
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {

    // 1. Calendario (Tu Home actual)
    object Calendario : Screen("calendario", "Calendario", Icons.Default.DateRange)

    // 2. Tareas (Podría ser el Inbox o una lista simple)
    object Tareas : Screen("tareas", "Tareas", Icons.Default.CheckCircle)

    // 3. Horarios (El Wizard o configuración de materias)
    object Horarios : Screen("horarios", "Horarios", Icons.Default.EditCalendar)

    // 4. Míos (Perfil o Ajustes)
    object Mios : Screen("mios", "Míos", Icons.Default.Person)
}
