package com.example.app_de_gestion_de_horarios_movil.domain.usecase

import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.domain.repository.ITaskRepository

class ToggleTaskCompletionUseCase(private val repository: ITaskRepository) {
    suspend operator fun invoke(task: Task) {
        // Creamos una copia invertida del estado actual
        val updatedTask = task.copy(isCompleted = !task.isCompleted)
        // Guardar sobrescribe la anterior gracias a OnConflictStrategy.REPLACE
        repository.saveTask(updatedTask)
    }
}