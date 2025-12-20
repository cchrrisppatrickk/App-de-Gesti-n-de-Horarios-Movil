package com.example.app_de_gestion_de_horarios_movil.domain.repository

import com.example.app_de_gestion_de_horarios_movil.domain.model.Task

interface IAlarmScheduler {
    // Programa todas las alertas configuradas en la tarea
    fun schedule(task: Task)

    // Cancela todas las alertas de la tarea (útil al borrar o editar)
    fun cancel(task: Task)
}