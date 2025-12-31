package com.example.app_de_gestion_de_horarios_movil.ui.components

import kotlinx.datetime.LocalTime

// Función existente
//fun LocalTime.toUiString(): String {
//    val amPm = if (hour >= 12) "PM" else "AM"
//    val hour12 = when {
//        hour == 0 -> 12
//        hour > 12 -> hour - 12
//        else -> hour
//    }
//    val m = minute.toString().padStart(2, '0')
//    return "$hour12:$m $amPm"
//}

// Ya que estamos, agrega esta si la usas para la UI
fun LocalTime.toUiString(): String {
    val amPm = if (this.hour < 12) "AM" else "PM"
    val hour12 = if (this.hour % 12 == 0) 12 else this.hour % 12
    return String.format("%02d:%02d %s", hour12, this.minute, amPm)
}


// --- AGREGA ESTA NUEVA FUNCIÓN ---
fun LocalTime.plusMinutes(minutes: Int): LocalTime {
    val totalMinutes = (this.hour * 60 + this.minute) + minutes
    // Lógica para rotar el reloj si pasa de 24h
    val normalizedMinutes = if (totalMinutes < 0) (24 * 60) + totalMinutes else totalMinutes

    val newHour = (normalizedMinutes / 60) % 24
    val newMinute = normalizedMinutes % 60
    return LocalTime(newHour, newMinute)
}

