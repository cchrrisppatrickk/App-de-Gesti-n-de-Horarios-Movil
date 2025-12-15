package com.example.app_de_gestion_de_horarios.ui.features.home

import com.example.app_de_gestion_de_horarios.domain.model.Task
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class HomeUiState(
    // La fecha seleccionada en el calendario superior
    val selectedDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,

    // La lista de tareas para esa fecha
    val tasks: List<Task> = emptyList(),

    // Indicador de carga (útil cuando cambiamos de día)
    val isLoading: Boolean = false,

    // Mensaje de error si algo falla (opcional)
    val errorMessage: String? = null
)