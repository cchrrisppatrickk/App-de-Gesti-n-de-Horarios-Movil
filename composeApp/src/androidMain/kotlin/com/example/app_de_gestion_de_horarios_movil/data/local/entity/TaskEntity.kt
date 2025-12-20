package com.example.app_de_gestion_de_horarios_movil.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.app_de_gestion_de_horarios_movil.domain.model.NotificationType
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task


import kotlinx.datetime.LocalDateTime

@Entity(
    tableName = "tasks",
    // INDICES: Crítico para el rendimiento.
    // Creamos un índice compuesto en start_time y end_time para
    // que la consulta "dame las tareas de hoy" sea instantánea.
    indices = [
        Index(value = ["start_time", "end_time"]),
        Index(value = ["group_id"]) // Para borrar semestres enteros rápido
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = false)
    val id: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String?,

    // Guardamos las fechas como String ISO-8601 (Ej: "2025-10-14T09:00:00")
    // Esto hace que sean legibles si inspeccionas la DB manualmente.
    @ColumnInfo(name = "start_time")
    val startTime: String,

    @ColumnInfo(name = "end_time")
    val endTime: String,

    @ColumnInfo(name = "is_all_day")
    val isAllDay: Boolean,

    @ColumnInfo(name = "icon_id")
    val iconId: String,

    @ColumnInfo(name = "color_hex")
    val colorHex: String,

    @ColumnInfo(name = "group_id")
    val groupId: String?,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,

    @ColumnInfo(name = "is_inbox_item")
    val isInboxItem: Boolean,

    // --- NUEVA COLUMNA ---
    // Room usará el Converter automáticamente para esto
    @ColumnInfo(name = "active_alerts")
    val activeAlerts: List<NotificationType>


) {
    /**
     * Función Maper: Convierte esta Entidad de Base de Datos
     * al Modelo de Dominio que usa la UI.
     */
    fun toDomain(): Task {
        return Task(
            id = id,
            title = title,
            description = description,
            // Parseamos el String de vuelta a LocalDateTime
            startTime = LocalDateTime.parse(startTime),
            endTime = LocalDateTime.parse(endTime),
            isAllDay = isAllDay,
            iconId = iconId,
            colorHex = colorHex,
            groupId = groupId,
            isCompleted = isCompleted,
            isInboxItem = isInboxItem,
            // Mapeo directo
            activeAlerts = activeAlerts
        )
    }
}

/**
 * Función de Extensión para hacer el camino inverso:
 * Convertir de Modelo de Dominio -> Entidad de Base de Datos
 */
fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        description = description,
        startTime = startTime.toString(), // Serializa a ISO String
        endTime = endTime.toString(),
        isAllDay = isAllDay,
        iconId = iconId,
        colorHex = colorHex,
        groupId = groupId,
        isCompleted = isCompleted,
        isInboxItem = isInboxItem,
        // Mapeo directo
        activeAlerts = activeAlerts
    )
}