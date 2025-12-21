package com.example.app_de_gestion_de_horarios_movil.ui.features.calendar

import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import kotlinx.datetime.LocalDate

data class CalendarUiState(
    val currentMonth: LocalDate, // Usamos el día 1 del mes como referencia
    val selectedDate: LocalDate, // El día seleccionado por el usuario
    val tasks: Map<LocalDate, List<Task>> = emptyMap(), // Mapa: Fecha -> Lista de Tareas
    val isLoading: Boolean = false
)