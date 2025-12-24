package com.example.app_de_gestion_de_horarios_movil.ui.features.calendar

import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.ui.features.calendar.model.CalendarViewMode
import kotlinx.datetime.LocalDate

data class CalendarUiState(
    val currentMonth: LocalDate,
    val selectedDate: LocalDate,
    val tasks: Map<LocalDate, List<Task>> = emptyMap(),
    val isLoading: Boolean = false,
    val viewMode: CalendarViewMode = CalendarViewMode.MONTH // <--- NUEVO CAMPO
)