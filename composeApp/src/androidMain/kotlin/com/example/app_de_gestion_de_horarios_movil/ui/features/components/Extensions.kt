package com.example.app_de_gestion_de_horarios_movil.ui.components

import kotlinx.datetime.LocalTime

// Función de extensión global para formatear la hora
fun LocalTime.toUiString(): String {
    val amPm = if (hour >= 12) "PM" else "AM"
    val hour12 = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val m = minute.toString().padStart(2, '0')
    return "$hour12:$m $amPm"
}