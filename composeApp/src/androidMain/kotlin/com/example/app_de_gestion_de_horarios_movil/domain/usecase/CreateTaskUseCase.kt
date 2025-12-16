package com.example.app_de_gestion_de_horarios_movil.domain.usecase

import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.domain.repository.ITaskRepository
import kotlinx.datetime.LocalDateTime
import java.util.UUID

class CreateTaskUseCase(
    private val repository: ITaskRepository
) {
    /**
     * Recibe los datos crudos del formulario, valida y guarda.
     * Retorna Result.success() si todo salió bien o Result.failure() con el error.
     */
    suspend operator fun invoke(
        title: String,
        description: String?,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        iconId: String = "ic_default", // Valor por defecto
        colorHex: String = "#3498DB"   // Azul por defecto
    ): Result<Unit> {

        // 1. Validaciones
        if (title.isBlank()) {
            return Result.failure(Exception("El título no puede estar vacío"))
        }
        if (endTime <= startTime) {
            return Result.failure(Exception("La hora de fin debe ser posterior al inicio"))
        }

        // 2. Construcción del objeto Task
        val newTask = Task(
            id = UUID.randomUUID().toString(), // Generamos ID único
            title = title.trim(),
            description = description?.trim(),
            startTime = startTime,
            endTime = endTime,
            isAllDay = false, // Por ahora simple
            iconId = iconId,
            colorHex = colorHex,
            isCompleted = false,
            isInboxItem = false
        )

        // 3. Guardado en Repositorio
        return try {
            repository.saveTask(newTask)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}