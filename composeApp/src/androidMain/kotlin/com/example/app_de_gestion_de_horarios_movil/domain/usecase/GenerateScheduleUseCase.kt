package com.example.app_de_gestion_de_horarios_movil.domain.usecase

import com.example.app_de_gestion_de_horarios_movil.domain.model.SubjectConfig
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.domain.repository.ITaskRepository
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.plus
import java.util.UUID

class GenerateScheduleUseCase(
    private val repository: ITaskRepository
) {

    suspend operator fun invoke(
        startDate: LocalDate,
        endDate: LocalDate,
        subjects: List<SubjectConfig>
    ): Result<Int> { // Retorna la cantidad de tareas creadas

        if (endDate < startDate) {
            return Result.failure(Exception("La fecha fin debe ser posterior al inicio"))
        }

        val tasksToInsert = mutableListOf<Task>()
        val semesterGroupId = UUID.randomUUID().toString() // ID para agrupar todo este ciclo

        // --- ALGORITMO DE GENERACIÓN ---

        // Iterador de fecha (Empezamos en el día 1)
        var currentDate = startDate

        while (currentDate <= endDate) {
            val currentDayOfWeek = currentDate.dayOfWeek // Ej: MONDAY

            // Revisamos cada materia configurada
            for (subject in subjects) {
                // Revisamos los horarios de esa materia
                for (schedule in subject.schedules) {

                    // ¿Toca clase hoy? (Coincide el día de la semana)
                    if (schedule.dayOfWeek == currentDayOfWeek) {

                        // Crear la Tarea concreta para HOY
                        val newTask = Task(
                            id = UUID.randomUUID().toString(),
                            groupId = semesterGroupId, // ¡Importante para agruparlas!
                            title = subject.name,
                            description = "Clase generada automáticamente",

                            // Combinar fecha actual + hora del horario
                            startTime = LocalDateTime(currentDate, schedule.startTime),
                            endTime = LocalDateTime(currentDate, schedule.endTime),

                            iconId = subject.iconId,
                            colorHex = subject.colorHex,
                            isCompleted = false
                        )
                        tasksToInsert.add(newTask)
                    }
                }
            }

            // Avanzar al siguiente día
            currentDate = currentDate.plus(DatePeriod(days = 1))
        }

        // --- GUARDADO MASIVO ---
        if (tasksToInsert.isNotEmpty()) {
            repository.saveTasks(tasksToInsert)
            return Result.success(tasksToInsert.size)
        } else {
            return Result.failure(Exception("No se generaron clases. Verifica los días seleccionados."))
        }
    }
}