package com.example.app_de_gestion_de_horarios_movil.domain.model

enum class NotificationType {
    AT_START,            // Al inicio
    AT_END,              // Al finalizar
    FIFTEEN_MIN_BEFORE,  // 15 minutos antes
    CUSTOM               // Reservado para lógica futura
}