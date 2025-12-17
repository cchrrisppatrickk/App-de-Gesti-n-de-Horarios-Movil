package com.example.app_de_gestion_de_horarios_movil.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimelineTaskRow(
    task: Task,
    isFirst: Boolean,
    isLast: Boolean,
    onToggleCompletion: () -> Unit, // <--- NUEVO PARÁMETRO
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val formattedTime = remember(task.startTime) {
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
        // COLUMNA 1: HORA
        Box(
            modifier = Modifier
                .width(50.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        // COLUMNA 2: NODO Y LÍNEA
        TimelineNode(
            colorHex = task.colorHex,
            isFirst = isFirst,
            isLast = isLast,
            modifier = Modifier.fillMaxHeight()
        )

        // COLUMNA 3: TARJETA
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 16.dp, bottom = 4.dp)
        ) {
            TaskCard(
                task = task,

                // CAMBIO AQUÍ:
                // Antes: task.durationMinutes >= TimelineConfig.SHORT_TASK_THRESHOLD
                // Ahora: true. (Porque incluso 15 min tendrá altura suficiente para verse bien)
                isVisualymediumOrLarge = true,

                onToggleCompletion = onToggleCompletion,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}