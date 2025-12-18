package com.example.app_de_gestion_de_horarios_movil.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.ColumnInfo
import com.example.app_de_gestion_de_horarios_movil.data.local.entity.TaskEntity

import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // 1. INSERCIÓN
    // OnConflictStrategy.REPLACE: Si insertas una tarea con un ID que ya existe,
    // la sobrescribe (útil para ediciones).
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    // Inserción masiva (Para cuando implementemos el "Wizard" de horarios)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    // 2. ACTUALIZACIÓN
    @Update
    suspend fun updateTask(task: TaskEntity)

    // 3. ELIMINACIÓN
    @Delete
    suspend fun deleteTask(task: TaskEntity)

    // Borrar todo un semestre/horario por su ID de grupo
    @Query("DELETE FROM tasks WHERE group_id = :groupId")
    suspend fun deleteTasksByGroupId(groupId: String)


    // 4. CONSULTAS (QUERIES)

    // Obtener una tarea específica
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?

    /**
     * CRUCIAL: Obtener tareas para la Cronología (Timeline).
     * Retorna un Flow: Si la base de datos cambia (ej: entra una nueva tarea),
     * la UI se actualiza sola automáticamente en tiempo real.
     *
     * @param startRange: Ej "2025-10-14T00:00:00"
     * @param endRange:   Ej "2025-10-14T23:59:59"
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE start_time >= :startRange AND start_time <= :endRange 
        ORDER BY start_time ASC
    """)
    fun getTasksForDateRange(startRange: String, endRange: String): Flow<List<TaskEntity>>

    // Obtener tareas pendientes (Inbox)
    @Query("SELECT * FROM tasks WHERE is_inbox_item = 1 AND is_completed = 0")
    fun getInboxTasks(): Flow<List<TaskEntity>>


    // --- NUEVO PARA EDICIÓN MASIVA ---
    @Query("SELECT * FROM tasks WHERE group_id = :groupId")
    suspend fun getTasksByGroupId(groupId: String): List<TaskEntity>


    /**
     * Consulta optimizada para el Calendario (StripCalendar).
     * Solo devuelve las columnas necesarias para pintar los puntitos.
     */
    @Query("""
        SELECT start_time, color_hex 
        FROM tasks 
        WHERE start_time >= :startRange AND start_time <= :endRange
    """)


    fun getTaskColorsForRange(startRange: String, endRange: String): Flow<List<TaskColorTuple>>
}


// Clase auxiliar (Tuple) para que Room mapee solo estas 2 columnas
// Colócala en el mismo archivo o en uno de utilidades
data class TaskColorTuple(
    @ColumnInfo(name = "start_time") val startTime: String, // Mapea start_time (SQL) -> startTime (Kotlin)
    @ColumnInfo(name = "color_hex") val colorHex: String    // Mapea color_hex (SQL) -> colorHex (Kotlin)
)