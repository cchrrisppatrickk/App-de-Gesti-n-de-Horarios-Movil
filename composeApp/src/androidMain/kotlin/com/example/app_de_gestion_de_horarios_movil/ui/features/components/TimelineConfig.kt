package com.example.app_de_gestion_de_horarios_movil.ui.components

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp

object TimelineConfig {

    // --- 1. DEFINICIÓN DE TIEMPOS (HITOS) ---
    const val TIME_NORMAL = 60L   // 1 Hora
    const val TIME_MEDIUM = 120L  // 2 Horas
    const val TIME_LARGE = 180L   // 3 Horas (Tope de crecimiento)

    // --- 2. DEFINICIÓN DE ALTURAS VISUALES ---
    // Altura "Piso": Para tareas de 0 a 60 min. Espacio justo para Título + Hora.
    val HEIGHT_NORMAL = 100.dp

    // Altura "Intermedia": Para tareas de 2 horas. Cabe una descripción breve.
    val HEIGHT_MEDIUM = 200.dp

    // Altura "Techo": Para tareas de 3 horas o más. Espacio completo.
    val HEIGHT_LARGE = 300.dp

    /**
     * Algoritmo de 3 Niveles:
     * Nivel 1 (0-60m): Altura Fija Normal.
     * Nivel 2 (60-120m): Crece suavemente de Normal a Media.
     * Nivel 3 (120-180m): Crece suavemente de Media a Grande.
     * Nivel 4 (180m+): Altura Fija Grande.
     */
    fun calculateHeight(durationMinutes: Long): androidx.compose.ui.unit.Dp {
        return when {
            // CASO A: Tarea Normal (0 a 60 min) -> Altura Fija
            durationMinutes <= TIME_NORMAL -> HEIGHT_NORMAL

            // CASO B: Transición Normal -> Media (60 a 120 min)
            durationMinutes < TIME_MEDIUM -> {
                val progress = (durationMinutes - TIME_NORMAL).toFloat()
                val range = (TIME_MEDIUM - TIME_NORMAL).toFloat()
                val fraction = progress / range
                lerp(HEIGHT_NORMAL, HEIGHT_MEDIUM, fraction)
            }

            // CASO C: Transición Media -> Grande (120 a 180 min)
            durationMinutes < TIME_LARGE -> {
                val progress = (durationMinutes - TIME_MEDIUM).toFloat()
                val range = (TIME_LARGE - TIME_MEDIUM).toFloat()
                val fraction = progress / range
                lerp(HEIGHT_MEDIUM, HEIGHT_LARGE, fraction)
            }

            // CASO D: Tarea Gigante (180 min en adelante) -> Altura Tope
            else -> HEIGHT_LARGE
        }
    }

    /**
     * (Opcional) Helper para saber en qué categoría cae la tarea.
     * Útil si quieres cambiar el tamaño de la fuente o iconos según el tamaño.
     */
    fun getTaskSizeCategory(durationMinutes: Long): TaskSize {
        return when {
            durationMinutes <= TIME_NORMAL -> TaskSize.Normal
            durationMinutes <= TIME_MEDIUM -> TaskSize.Medium
            else -> TaskSize.Large
        }
    }

    enum class TaskSize { Normal, Medium, Large }
}