package com.example.app_de_gestion_de_horarios_movil.ui.features.home

import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.LocalDateTime // Usamos Java Time para los cálculos internos del hueco

// 1. NUEVA INTERFAZ: Define qué puede haber en la línea de tiempo
sealed interface TimelineItem {
    // Caso A: Es una tarea normal
    data class TaskItem(val task: Task) : TimelineItem

    // Caso B: Es un hueco libre
    data class GapItem(
        val start: LocalDateTime,
        val end: LocalDateTime,
        val durationMinutes: Long
    ) : TimelineItem
}

data class HomeUiState(
    val selectedDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,

    // 2. CAMBIO: Ya no es solo List<Task>, ahora es la lista mixta
    // Mantenemos 'tasks' por compatibilidad si lo necesitas, pero la UI usará 'timelineItems'
    val tasks: List<Task> = emptyList(),
    val timelineItems: List<TimelineItem> = emptyList(), // <--- NUEVO CAMPO PRINCIPAL

    val isLoading: Boolean = false,
    val errorMessage: String? = null
)