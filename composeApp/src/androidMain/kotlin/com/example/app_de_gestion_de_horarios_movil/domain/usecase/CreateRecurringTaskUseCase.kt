package com.example.app_de_gestion_de_horarios_movil.domain.usecase

import com.example.app_de_gestion_de_horarios_movil.domain.model.RecurrenceMode
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.domain.repository.ITaskRepository
import kotlinx.datetime.*
import java.util.UUID

class CreateRecurringTaskUseCase(
    private val repository: ITaskRepository
) {
    suspend operator fun invoke(
        baseTask: Task, // La tarea con los datos del formulario
        mode: RecurrenceMode,
        recurrenceEndDate: LocalDate, // Hasta cuándo repetir
        selectedDays: List<DayOfWeek> = emptyList() // Solo para CUSTOM
    ): Result<Unit> {
        return try {
            val tasksToInsert = mutableListOf<Task>()
            // Generamos un ID de grupo único para vincular todas las copias
            val groupId = UUID.randomUUID().toString()

            var currentDate = baseTask.startTime.date

            // Bucle: Desde hoy hasta la fecha límite
            while (currentDate <= recurrenceEndDate) {

                if (isDateValidForRecurrence(currentDate, baseTask.startTime.date, mode, selectedDays)) {
                    // Clonamos la tarea ajustando la fecha
                    val newTask = baseTask.copy(
                        id = UUID.randomUUID().toString(), // ID único para cada instancia
                        groupId = groupId, // El mismo Grupo ID para todas
                        startTime = LocalDateTime(currentDate, baseTask.startTime.time),
                        endTime = LocalDateTime(currentDate, baseTask.endTime.time)
                    )
                    tasksToInsert.add(newTask)
                }

                // Avanzamos un día
                currentDate = currentDate.plus(DatePeriod(days = 1))
            }

            // Guardado masivo (Reutilizamos el método del Repo que ya tienes)
            repository.saveTasks(tasksToInsert)
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isDateValidForRecurrence(
        currentDate: LocalDate,
        startDate: LocalDate,
        mode: RecurrenceMode,
        selectedDays: List<DayOfWeek>
    ): Boolean {
        return when (mode) {
            RecurrenceMode.DAILY -> true
            RecurrenceMode.WEEKLY -> currentDate.dayOfWeek == startDate.dayOfWeek
            RecurrenceMode.CUSTOM -> selectedDays.contains(currentDate.dayOfWeek)
            RecurrenceMode.ONCE -> currentDate == startDate
        }
    }
}