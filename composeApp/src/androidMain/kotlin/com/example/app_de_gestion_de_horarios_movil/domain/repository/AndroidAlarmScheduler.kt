package com.example.app_de_gestion_de_horarios_movil.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.app_de_gestion_de_horarios_movil.domain.model.NotificationType
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.domain.repository.IAlarmScheduler
import com.example.app_de_gestion_de_horarios_movil.ui.features.notifications.AlarmReceiver
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime

class AndroidAlarmScheduler(
    private val context: Context
) : IAlarmScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(task: Task) {
        // Si no hay alertas activas, no hacemos nada
        if (task.activeAlerts.isEmpty()) return

        // Programamos cada tipo de alerta
        task.activeAlerts.forEach { type ->
            scheduleNotification(task, type)
        }
    }

    override fun cancel(task: Task) {
        task.activeAlerts.forEach { type ->
            val pendingIntent = createPendingIntent(task, type)
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun scheduleNotification(task:  Task, type: NotificationType) {
        // 1. Calcular el momento exacto (en milisegundos)
        val triggerTime = when (type) {
            NotificationType.AT_START -> task.startTime
            NotificationType.AT_END -> task.endTime
            NotificationType.FIFTEEN_MIN_BEFORE -> {
                // Restamos 15 minutos usando Java Time o Kotlinx DateTime
                val instant = task.startTime.toInstant(TimeZone.currentSystemDefault())
                instant.minus(kotlinx.datetime.DateTimeUnit.MINUTE * 15)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
            }
            else -> return // Custom no implementado aún
        }

        // Convertir a milisegundos Epoch (que es lo que pide Android)
        val triggerMillis = triggerTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

        // Validación: Si la hora ya pasó, no programamos nada
        if (triggerMillis <= System.currentTimeMillis()) return

        // 2. Crear el Intent y PendingIntent
        val pendingIntent = createPendingIntent(task, type)

        // 3. Programar la Alarma
        // setExactAndAllowWhileIdle: Garantiza que suene incluso si el tlf está en modo ahorro (Doze)
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Aquí caemos si no tenemos el permiso SCHEDULE_EXACT_ALARM
            e.printStackTrace()
        }
    }

    private fun createPendingIntent(task: Task, type: NotificationType): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TASK_ID, task.id)
            putExtra(AlarmReceiver.EXTRA_TITLE, task.title)

            // Mensaje personalizado según el tipo
            val msg = when(type) {
                NotificationType.AT_START -> "¡Comienza ahora!"
                NotificationType.AT_END -> "Ha finalizado."
                NotificationType.FIFTEEN_MIN_BEFORE -> "Comienza en 15 minutos."
                else -> "Recordatorio."
            }
            putExtra(AlarmReceiver.EXTRA_MESSAGE, msg)
        }

        // Generamos un ID único combinando el hash de la tarea y el tipo de alerta.
        // Esto permite tener varias alarmas diferentes para la misma tarea.
        val uniqueRequestCode = task.id.hashCode() + type.ordinal

        return PendingIntent.getBroadcast(
            context,
            uniqueRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}