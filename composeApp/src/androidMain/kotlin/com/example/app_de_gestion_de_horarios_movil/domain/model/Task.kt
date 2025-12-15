package com.example.app_de_gestion_de_horarios.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/**
 * Representa una tarea o evento en la cronología.
 * Esta clase es "agnóstica": no sabe si viene de una DB, de Internet o de memoria.
 */
data class Task(
    val id: String,                 // UUID único
    val title: String,              // Ej: "Clase de Matemáticas"
    val description: String?,       // Ej: "Traer calculadora" (Puede ser nulo)

    // Temporalidad
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val isAllDay: Boolean = false,

    // Estilo y Metadatos
    val iconId: String,             // Identificador del recurso (ej: "ic_book")
    val colorHex: String,           // Ej: "#FF5733"

    // Relaciones y Estado
    val groupId: String? = null,    // Si pertenece a un Horario semestral
    val isCompleted: Boolean = false,
    val isInboxItem: Boolean = false // Si está en la bandeja de entrada (sin fecha)
) {
    // Propiedad calculada útil para la UI (CORREGIDA)
    // Calcula la diferencia real en minutos entre las dos fechas
    val durationMinutes: Long
        get() {
            // Convertimos a "Instant" (tiempo absoluto) usando UTC para poder restar
            val startInstant = startTime.toInstant(TimeZone.UTC)
            val endInstant = endTime.toInstant(TimeZone.UTC)

            // Restamos y obtenemos los minutos completos
            return (endInstant - startInstant).inWholeMinutes
        }
}