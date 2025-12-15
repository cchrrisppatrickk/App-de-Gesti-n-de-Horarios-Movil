package com.example.app_de_gestion_de_horarios.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

/**
 * Configuración de una MATERIA (Ej: "Física")
 * Contiene una lista de horarios semanales (Ej: Lunes 8am y Jueves 10am)
 */
data class SubjectConfig(
    val id: String, // UUID temporal
    val name: String,
    val colorHex: String,
    val iconId: String = "ic_book",
    val schedules: List<WeeklySchedule> // Lista de horarios
)

/**
 * Un bloque horario específico (Ej: "Lunes de 08:00 a 10:00")
 */
data class WeeklySchedule(
    val dayOfWeek: DayOfWeek, // Enum de Kotlin (MONDAY, TUESDAY...)
    val startTime: LocalTime,
    val endTime: LocalTime
)