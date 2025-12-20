package com.example.app_de_gestion_de_horarios_movil.data.local.converter

import androidx.room.TypeConverter
import com.example.app_de_gestion_de_horarios_movil.domain.model.NotificationType

class Converters {

    // Convierte la lista del modelo a un String para la BD
    // Ej: [AT_START, AT_END] -> "AT_START,AT_END"
    @TypeConverter
    fun fromNotificationList(value: List<NotificationType>?): String {
        if (value.isNullOrEmpty()) return ""
        return value.joinToString(separator = ",") { it.name }
    }

    // Convierte el String de la BD a una lista para el modelo
    // Ej: "AT_START,AT_END" -> [AT_START, AT_END]
    @TypeConverter
    fun toNotificationList(value: String?): List<NotificationType> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split(",").map {
            try {
                NotificationType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                // Si en el futuro borramos un tipo de Enum, esto evita que la app crashee
                NotificationType.CUSTOM
            }
        }
    }
}