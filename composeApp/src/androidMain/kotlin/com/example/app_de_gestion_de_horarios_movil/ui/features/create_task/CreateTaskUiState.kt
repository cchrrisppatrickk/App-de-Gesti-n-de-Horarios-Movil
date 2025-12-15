package com.example.app_de_gestion_de_horarios.ui.features.create_task

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class CreateTaskUiState(
    // Campos del formulario
    val title: String = "",
    val description: String = "",

    // Por defecto hoy
    val selectedDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,

    // Por defecto hora actual
    val startTime: LocalTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time,

    // Por defecto 1 hora después
    val endTime: LocalTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time,

    val isLoading: Boolean = false,
    val isTaskSaved: Boolean = false, // Para saber cuándo cerrar el modal
    val error: String? = null
)