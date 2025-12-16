package com.example.app_de_gestion_de_horarios_movil.ui.components

import androidx.compose.ui.unit.dp

object TimelineConfig {
    // 1. Umbrales de Tiempo (En minutos)
    const val SHORT_TASK_THRESHOLD = 30L  // Menos de esto es "corta"
    const val MAX_VISUAL_DURATION = 120L  // A partir de 2 horas, la tarjeta ya no crece más

    // 2. Alturas Definidas (En DP)
    val COMPACT_HEIGHT = 50.dp        // Altura fija para tareas cortas
    val BASE_HEIGHT_PER_HOUR = 60.dp  // Qué tan alta se ve una hora estándar

    // Altura máxima absoluta que puede tener una tarjeta (Tope estético)
    // Aunque la tarea dure 5 horas, visualmente ocupará como si fueran 2 horas (aprox 160dp)
    val MAX_CARD_HEIGHT = 120.dp

    /**
     * Calcula la altura dinámica basada en la duración.
     */
    fun calculateHeight(durationMinutes: Long): androidx.compose.ui.unit.Dp {
        return when {
            // Caso A: Tarea Corta (View Compacta)
            durationMinutes < SHORT_TASK_THRESHOLD -> COMPACT_HEIGHT

            // Caso B: Tarea Larga (Excede el tope visual)
            durationMinutes >= MAX_VISUAL_DURATION -> MAX_CARD_HEIGHT

            // Caso C: Tarea Media (Escala Proporcional)
            else -> {
                // Regla de 3 simple: (Duración / 60) * AlturaPorHora
                val calculated = (durationMinutes / 60f) * BASE_HEIGHT_PER_HOUR.value
                calculated.dp
            }
        }
    }
}