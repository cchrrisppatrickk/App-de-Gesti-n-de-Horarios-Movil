package com.example.app_de_gestion_de_horarios_movil.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object TaskIcons {
    // Mapa: ID (String para BD) -> Icono (Gráfico para UI)
    val availableIcons = mapOf(
        "default" to Icons.Default.Event,
        "study" to Icons.Default.School,
        "work" to Icons.Default.Work,
        "sport" to Icons.Default.SportsBasketball, // O SportsGym si tienes la dependencia extended
        "health" to Icons.Default.LocalHospital,
        "shopping" to Icons.Default.ShoppingCart,
        "finance" to Icons.Default.AttachMoney,
        "travel" to Icons.Default.Flight,
        "leisure" to Icons.Default.SportsEsports,
        "social" to Icons.Default.Groups
    )

    // Función auxiliar para recuperar el icono seguro (si no existe, devuelve default)
    fun getIconById(id: String): ImageVector {
        return availableIcons[id] ?: Icons.Default.Event
    }
}