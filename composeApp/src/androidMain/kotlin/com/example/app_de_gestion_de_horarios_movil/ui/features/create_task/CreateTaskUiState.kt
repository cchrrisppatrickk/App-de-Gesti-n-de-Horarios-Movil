package com.example.app_de_gestion_de_horarios_movil.ui.features.create_task

import TaskType
import com.example.app_de_gestion_de_horarios_movil.domain.model.NotificationType
import com.example.app_de_gestion_de_horarios_movil.domain.model.RecurrenceMode
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

data class CreateTaskUiState(
    val taskId: String? = null,
    val title: String = "",
    val description: String = "",
    val selectedDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val startTime: LocalTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time,
    val endTime: LocalTime = Clock.System.now()
        .plus(1, DateTimeUnit.HOUR)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .time,

    val selectedColorHex: String = "#3498DB",
    val selectedIconId: String = "ic_default",

    val recurrenceMode: RecurrenceMode = RecurrenceMode.ONCE,
    val recurrenceEndDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.plus(
        DatePeriod(months = 1)
    ),
    val selectedRecurrenceDays: List<DayOfWeek> = emptyList(),

    // --- NUEVO CAMPO FASE 4 ---
    // Almacena las alertas seleccionadas en el formulario
    val selectedAlerts: List<NotificationType> = emptyList(),
    // --------------------------

    val isLoading: Boolean = false,
    val isTaskSaved: Boolean = false,
    val error: String? = null,

    // --- NUEVOS CAMPOS ---
    val entryType: TaskType = TaskType.TASK, // Discriminador (Tarea vs Evento)
    val isAllDay: Boolean = false,           // Switch "Todo el día"
    // ---------------------
)