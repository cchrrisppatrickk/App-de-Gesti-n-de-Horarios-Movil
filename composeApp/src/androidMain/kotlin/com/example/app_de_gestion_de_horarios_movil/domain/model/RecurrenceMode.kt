package com.example.app_de_gestion_de_horarios_movil.domain.model

enum class RecurrenceMode {
    ONCE,       // Una sola vez (Default)
    DAILY,      // Todos los días
    WEEKLY,     // Una vez a la semana (el mismo día que la fecha de inicio)
    CUSTOM      // Días específicos (L, M, V...)
}