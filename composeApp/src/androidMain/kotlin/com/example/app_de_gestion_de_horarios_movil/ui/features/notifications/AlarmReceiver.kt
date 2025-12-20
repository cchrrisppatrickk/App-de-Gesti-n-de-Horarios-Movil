package com.example.app_de_gestion_de_horarios_movil.ui.features.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.app_de_gestion_de_horarios_movil.MainActivity
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "task_reminders_channel"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // 1. Verificar Permiso (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return // Si el usuario quitó el permiso, no hacemos nada.
            }
        }

        // 2. Extraer datos de la Tarea
        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Recordatorio"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Tienes una tarea pendiente"

        // 3. Crear el Canal de Notificación (Obligatorio en Android 8.0+)
        createNotificationChannel(context)

        // 4. Configurar qué pasa al tocar la notificación (Abrir la App)
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to_task_id", taskId) // Por si quieres navegar directo en el futuro
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(), // RequestCode único
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 5. Construir la Notificación Visual
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // CAMBIAR POR TU ICONO DE APP (R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Para que salga como banner
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Se borra al tocarla
            .build()

        // 6. Mostrarla
        // Usamos el ID del sistema hashcode para que no se sobrescriban si hay varias
        NotificationManagerCompat.from(context).notify(taskId.hashCode(), notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios de Tareas"
            val descriptionText = "Notificaciones para tus clases y eventos"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}