package com.example.app_de_gestion_de_horarios_movil.domain.usecase

import android.util.Log
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.domain.repository.ITaskRepository
import kotlinx.datetime.LocalDateTime

class UpdateTaskGroupUseCase(private val repository: ITaskRepository) {

    /**
     * @param groupId: El ID del grupo que queremos editar.
     * @param templateTask: La tarea con los NUEVOS datos (Título, Hora, Color, Icono...)
     */
    suspend operator fun invoke(groupId: String, templateTask: Task) {

        // 1. Traer todas las tareas originales del grupo
        val existingTasks = repository.getTasksByGroupId(groupId)

        if (existingTasks.isEmpty()) {
            Log.e("UpdateGroup", "No se encontraron tareas para el grupo: $groupId")
            return
        }

        // 2. Aplicar los cambios a cada tarea existente
        val updatedTasks = existingTasks.map { existing ->

            // FUSIÓN DE TIEMPO INTELIGENTE:
            // Fecha: Usamos la fecha ORIGINAL de la tarea (existing.date)
            // Hora: Usamos la hora NUEVA que puso el usuario (templateTask.time)

            val newStart = LocalDateTime(
                existing.startTime.date,
                templateTask.startTime.time
            )

            val newEnd = LocalDateTime(
                existing.endTime.date,
                templateTask.endTime.time
            )

            // Creamos la copia actualizada
            existing.copy(
                title = templateTask.title,
                description = templateTask.description,
                colorHex = templateTask.colorHex,
                iconId = templateTask.iconId,
                startTime = newStart,
                endTime = newEnd
                // groupId e id se mantienen intactos automáticamente
            )
        }

        // 3. Guardar todo el lote (Sobrescribe las anteriores)
        repository.saveTasks(updatedTasks)
    }
}