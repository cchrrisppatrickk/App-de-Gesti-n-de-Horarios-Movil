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
import androidx.compose.ui.text.font.FontWeight
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
    modifier: Modifier = Modifier
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val formattedTime = remember(task.startTime) {
        task.startTime.toJavaLocalDateTime().format(timeFormatter)
    }

    // --- CÁLCULO DE ALTURA SUTIL ---
    // Usamos la configuración centralizada.
    val rowHeight = remember(task.durationMinutes) {
        TimelineConfig.calculateHeight(task.durationMinutes)
    }

    Row(
        modifier = modifier
            .height(rowHeight) // Altura controlada
            .padding(vertical = 4.dp)
    ) {
        // COLUMNA 1: HORA
        // Alineamos la hora siempre arriba (Top) para que coincida con el inicio del bloque
        Box(
            modifier = Modifier
                .width(50.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter // Hora siempre arriba
        ) {
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp) // Ajuste fino visual
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
                // Pasamos la duración al Card para que decida si muestra detalles extra
                isVisualymediumOrLarge = task.durationMinutes >= TimelineConfig.SHORT_TASK_THRESHOLD,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}