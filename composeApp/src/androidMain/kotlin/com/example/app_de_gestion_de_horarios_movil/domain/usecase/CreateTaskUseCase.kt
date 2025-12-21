package com.example.app_de_gestion_de_horarios_movil.domain.usecase

import TaskType
import com.example.app_de_gestion_de_horarios_movil.domain.model.NotificationType
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task // <--- IMPORTANTE: Importar TaskType
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
        activeAlerts: List<NotificationType>,

        // --- NUEVOS PARÁMETROS AGREGADOS ---
        type: TaskType = TaskType.TASK, // Valor por defecto TASK para compatibilidad
        isAllDay: Boolean = false       // Valor por defecto false
    ): Result<Unit> {

        if (title.isBlank()) {
            return Result.failure(Exception("El título no puede estar vacío"))
        }

        // Validación: Solo validamos que fin > inicio si NO es tarea de todo el día.
        // Si es "Todo el día", la hora exacta no importa tanto, pero por sanidad mantenemos la lógica
        // o la relajamos según tu regla de negocio. Por ahora la dejamos igual.
        if (endTime <= startTime && !isAllDay) {
            return Result.failure(Exception("La hora de fin debe ser posterior al inicio"))
        }

        val newTask = Task(
            id = id ?: UUID.randomUUID().toString(),
            title = title.trim(),
            description = description?.trim(),
            startTime = startTime,
            endTime = endTime,

            // --- ASIGNACIÓN DE LOS NUEVOS VALORES ---
            isAllDay = isAllDay, // Ya no es 'false' fijo
            type = type,         // Asignamos el tipo (Evento o Tarea)

            iconId = iconId,
            colorHex = colorHex,
            isCompleted = false,
            isInboxItem = false,
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