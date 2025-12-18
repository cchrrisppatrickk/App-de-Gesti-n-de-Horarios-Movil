package com.example.app_de_gestion_de_horarios_movil.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import kotlinx.coroutines.delay
import kotlinx.datetime.toJavaLocalDateTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimelineTaskRow(
    task: Task,
    isFirst: Boolean,
    isLast: Boolean,
    onToggleCompletion: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Actualización cada 15 segundos para que el reloj sea preciso
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            delay(15_000L)
        }
    }

    // Calculamos el string de la hora actual (Ej: "14:34")
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val currentTimeString = remember(currentTime) {
        currentTime.format(timeFormatter)
    }

    // Cálculo de progreso (Igual que antes)
    val progress = remember(task, currentTime) {
        val start = task.startTime.toJavaLocalDateTime()
        val end = task.endTime.toJavaLocalDateTime()

        when {
            currentTime.isAfter(end) -> 1.0f
            currentTime.isBefore(start) -> 0.0f
            else -> {
                val totalMinutes = ChronoUnit.MINUTES.between(start, end).toFloat()
                val currentMinutes = ChronoUnit.MINUTES.between(start, currentTime).toFloat()
                if (totalMinutes > 0) (currentMinutes / totalMinutes).coerceIn(0f, 1f) else 0f
            }
        }
    }

    val taskStartTimeFormatted = remember(task.startTime) {
        task.startTime.toJavaLocalDateTime().format(timeFormatter)
    }

    val rowHeight = remember(task.durationMinutes) {
        TimelineConfig.calculateHeight(task.durationMinutes)
    }

    Row(
        modifier = modifier
            .height(rowHeight)
            .padding(vertical = 4.dp)
    ) {
        // COLUMNA 1: HORA ESTÁTICA (Inicio de tarea)
        Box(
            modifier = Modifier
                .width(50.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = taskStartTimeFormatted,
                style = MaterialTheme.typography.labelMedium,
                // Si la tarea está activa, pintamos la hora estática del mismo color
                color = if (progress > 0f && progress < 1f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        // COLUMNA 2: NODO Y LÍNEA (Con Hora Flotante)
        TimelineNode(
            colorHex = task.colorHex,
            isFirst = isFirst,
            isLast = isLast,
            progress = progress,
            currentTimeString = currentTimeString, // <--- PASAMOS LA HORA
            modifier = Modifier.fillMaxHeight()
        )

        // COLUMNA 3: TARJETA
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, end = 16.dp, bottom = 4.dp)
        ) {
            TaskCard(
                task = task,
                isVisualymediumOrLarge = true,
                onToggleCompletion = onToggleCompletion,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}