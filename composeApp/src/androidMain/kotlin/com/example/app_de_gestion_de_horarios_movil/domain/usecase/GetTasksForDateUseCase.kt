package com.example.app_de_gestion_de_horarios.domain.usecase

import com.example.app_de_gestion_de_horarios.domain.model.Task
import com.example.app_de_gestion_de_horarios.domain.repository.ITaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class GetTasksForDateUseCase(
    private val repository: ITaskRepository
) {
    /**
     * El operador 'invoke' permite llamar a la clase como si fuera una función:
     * val tareas = getTasksForDateUseCase(fecha)
     */
    operator fun invoke(date: LocalDate): Flow<List<Task>> {
        return repository.getTasksForDate(date).map { tasks ->
            // AQUÍ PONDREMOS LÓGICA DE NEGOCIO EN EL FUTURO
            // Por ejemplo:
            // 1. Filtrar tareas canceladas si no queremos verlas.
            // 2. Ordenar las tareas "All Day" primero.

            // Por ahora, ordenamos explícitamente por hora de inicio para asegurar el dibujo
            tasks.sortedBy { it.startTime }
        }
    }
}