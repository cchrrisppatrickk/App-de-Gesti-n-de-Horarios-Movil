package com.example.app_de_gestion_de_horarios_movil.domain.usecase

import com.example.app_de_gestion_de_horarios_movil.domain.model.NotificationType
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.domain.repository.ITaskRepository
import kotlinx.datetime.LocalDateTime
import java.util.UUID

class CreateTaskUseCase(
    private val repository: ITaskRepository
) {
    suspend operator fun invoke(
        id: String? = null,
        title: String,
        description: String?,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        iconId: String = "ic_default",
        colorHex: String = "#3498DB",
        // --- NUEVO PARÁMETRO ---
        activeAlerts: List<NotificationType>
    ): Result<Unit> {

        if (title.isBlank()) {
            return Result.failure(Exception("El título no puede estar vacío"))
        }
        if (endTime <= startTime) {
            return Result.failure(Exception("La hora de fin debe ser posterior al inicio"))
        }

        val newTask = Task(
            id = id ?: UUID.randomUUID().toString(),
            title = title.trim(),
            description = description?.trim(),
            startTime = startTime,
            endTime = endTime,
            isAllDay = false,
            iconId = iconId,
            colorHex = colorHex,
            isCompleted = false,
            isInboxItem = false,
            // --- ASIGNACIÓN ---
            activeAlerts = activeAlerts
        )

        return try {
            repository.saveTask(newTask)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}