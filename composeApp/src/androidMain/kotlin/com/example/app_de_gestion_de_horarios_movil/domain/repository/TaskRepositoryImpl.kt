package com.example.app_de_gestion_de_horarios_movil.data.repository

import com.example.app_de_gestion_de_horarios_movil.data.local.dao.TaskDao

import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.domain.repository.ITaskRepository
import com.example.app_de_gestion_de_horarios_movil.data.local.entity.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime

class TaskRepositoryImpl(
    private val dao: TaskDao
) : ITaskRepository {

    override fun getTasksForDate(date: LocalDate): Flow<List<Task>> {
        // 1. Calcular inicio y fin del día para la consulta SQL
        // Usamos .atTime(h, m, s) de kotlinx-datetime
        val startOfDay = date.atTime(0, 0, 0).toString()
        val endOfDay = date.atTime(23, 59, 59).toString()

        // 2. Llamar al DAO y transformar los datos (Map)
        return dao.getTasksForDateRange(startOfDay, endOfDay).map { entities ->
            // Convertimos la lista de Entities a lista de Domain Tasks
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTaskById(taskId: String): Task? {
        return dao.getTaskById(taskId)?.toDomain()
    }

    override suspend fun saveTask(task: Task) {
        // Convertimos Domain -> Entity antes de guardar
        dao.insertTask(task.toEntity())
    }

    override suspend fun deleteTask(taskId: String) {
        // Para borrar, primero buscamos la entidad (necesitamos todos sus datos para el @Delete de Room)
        // Ojo: Podríamos optimizar el DAO para borrar solo por ID con un @Query,
        // pero por seguridad de Room, buscamos y borramos.
        val entity = dao.getTaskById(taskId)
        if (entity != null) {
            dao.deleteTask(entity)
        }
    }

    override fun getInboxTasks(): Flow<List<Task>> {
        return dao.getInboxTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // --- NUEVA IMPLEMENTACIÓN ---
    override suspend fun deleteTasksByGroupId(groupId: String) {
        // Delegamos directamente al DAO, que ejecuta un DELETE WHERE groupId = :id
        dao.deleteTasksByGroupId(groupId)
    }
    // ----------------------------

    // --- IMPLEMENTACIÓN NUEVA ---
    override suspend fun getTasksByGroupId(groupId: String): List<Task> {
        return dao.getTasksByGroupId(groupId).map { it.toDomain() }
    }

    override suspend fun saveTasks(tasks: List<Task>) {
        // Convertimos la lista completa de Domain -> Entity
        val entities = tasks.map { it.toEntity() }
        dao.insertTasks(entities)
    }

    // En TaskRepositoryImpl:
    override fun getCalendarIndicators(startDate: LocalDate, endDate: LocalDate): Flow<Map<LocalDate, List<String>>> {
        val startStr = startDate.atTime(0, 0).toString()
        val endStr = endDate.atTime(23, 59).toString()

        return dao.getTaskColorsForRange(startStr, endStr).map { tuples ->
            // Transformamos la lista plana de SQL a un Mapa agrupado por día
            // Resultado: Map<2025-10-15, ["#FF0000", "#00FF00"]>
            tuples.groupBy(
                keySelector = {
                    // Parseamos el String ISO a LocalDate
                    LocalDateTime.parse(it.startTime).date
                },
                valueTransform = { it.colorHex }
            )
        }
    }

    override fun getTasksBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<Map<LocalDate, List<Task>>> {
        val startString = startDate.atTime(0, 0).toString()
        // Ajuste para fin del día en kotlinx
        val endString = endDate.atTime(23, 59, 59, 999_999_999).toString()

        // CAMBIO CRUCIAL: Usamos 'getAllTasksForDateRange' en lugar de 'getTasksForDateRange'
        return dao.getAllTasksForDateRange(startString, endString)
            .map { entities ->
                entities.map { it.toDomain() }
                    .groupBy { task -> task.startTime.date }
            }
    }

}