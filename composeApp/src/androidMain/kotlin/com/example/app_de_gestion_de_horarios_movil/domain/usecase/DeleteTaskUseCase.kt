package com.example.app_de_gestion_de_horarios_movil.domain.usecase

import com.example.app_de_gestion_de_horarios_movil.domain.repository.ITaskRepository

class DeleteTaskUseCase(private val repository: ITaskRepository) {

    // Opción 1: Borrar solo esta tarea (Mantenemos el operador invoke para compatibilidad)
    suspend operator fun invoke(taskId: String) {
        repository.deleteTask(taskId)
    }

    // Opción 2: Borrar todo el grupo (NUEVO)
    suspend fun deleteGroup(groupId: String) {
        repository.deleteTasksByGroupId(groupId)
    }
}