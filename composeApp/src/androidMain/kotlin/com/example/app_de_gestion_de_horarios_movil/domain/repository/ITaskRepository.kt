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
     * Elimina una tarea.
     */
    suspend fun deleteTask(taskId: String)

    /**
     * Obtiene las tareas de la bandeja de entrada (sin fecha asignada).
     */
    fun getInboxTasks(): Flow<List<Task>>

    // Nueva función para lotes
    suspend fun saveTasks(tasks: List<Task>)
}