package com.example.app_de_gestion_de_horarios_movil.ui.features.create_task

import com.example.app_de_gestion_de_horarios_movil.domain.model.RecurrenceMode
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

data class CreateTaskUiState(
    val taskId: String? = null, // Null = Creando nueva, String = Editando
    val title: String = "",
    val description: String = "",
    val selectedDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val startTime: LocalTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time,
    // Por defecto 1 hora después
    val endTime: LocalTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time.let {
        LocalTime(it.hour + 1, it.minute)
    },

    // --- NUEVOS CAMPOS ---
    val selectedColorHex: String = "#3498DB", // Azul por defecto
    val selectedIconId: String = "ic_default",
    // ---------------------

    // --- NUEVOS CAMPOS DE REPETICIÓN ---
    val recurrenceMode: RecurrenceMode = RecurrenceMode.ONCE,
    // Por defecto, repetir por 1 mes si se activa
    val recurrenceEndDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.plus(
        DatePeriod(months = 1)
    ),
    val selectedRecurrenceDays: List<DayOfWeek> = emptyList(), // Para custom
    // -----------------------------------

    val isLoading: Boolean = false,
    val isTaskSaved: Boolean = false,
    val error: String? = null
)