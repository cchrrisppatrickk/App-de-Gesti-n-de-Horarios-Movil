package com.example.app_de_gestion_de_horarios_movil.domain.repository

import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ITaskRepository {

    /**
     * Obtiene un flujo de tareas para una fecha específica.
     * Al ser un Flow, si agregas una tarea nueva, la lista se actualiza sola.
     */
    fun getTasksForDate(date: LocalDate): Flow<List<Task>>

    /**
     * Obtiene una tarea específica por su ID.
     */
    suspend fun getTaskById(taskId: String): Task?

    /**
     * Guarda (Inserta o Actualiza) una tarea.
     */
    suspend fun saveTask(task: Task)

    /**
     * Elimina una tarea individual por su ID.
     */
    suspend fun deleteTask(taskId: String)

    /**
     * Obtiene las tareas de la bandeja de entrada (sin fecha asignada).
     */
    fun getInboxTasks(): Flow<List<Task>>

    /**
     * NUEVO: Elimina todas las tareas que compartan el mismo GroupID.
     * Útil para borrar series recurrentes completas.
     */
    suspend fun deleteTasksByGroupId(groupId: String)

    // Nueva función para lotes
    suspend fun saveTasks(tasks: List<Task>)

    // --- NUEVO ---
    suspend fun getTasksByGroupId(groupId: String): List<Task>

    // En ITaskRepository:
    fun getCalendarIndicators(startDate: LocalDate, endDate: LocalDate): Flow<Map<LocalDate, List<String>>>
}