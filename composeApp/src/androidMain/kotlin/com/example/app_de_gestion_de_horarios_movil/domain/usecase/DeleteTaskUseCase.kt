package com.example.app_de_gestion_de_horarios_movil.domain.usecase

import com.example.app_de_gestion_de_horarios_movil.domain.repository.ITaskRepository

class DeleteTaskUseCase(private val repository: ITaskRepository) {
    suspend operator fun invoke(taskId: String) {
        repository.deleteTask(taskId)
    }
}