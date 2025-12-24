package com.example.app_de_gestion_de_horarios_movil.ui.features.calendar.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.ui.features.calendar.getContrastColor
import kotlinx.datetime.LocalDateTime

// Configuración de dimensiones
private val HOUR_HEIGHT = 60.dp
private val TIME_COLUMN_WIDTH = 50.dp

@Composable
fun DayView(
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(modifier = modifier.verticalScroll(scrollState)) {
        // Dibujamos las 24 horas del día
        Box(modifier = Modifier.fillMaxWidth().height(HOUR_HEIGHT * 24)) {

            // 1. LÍNEAS DE FONDO Y HORAS
            for (hour in 0..23) {
                val topOffset = HOUR_HEIGHT * hour

                // Hora (Columna Izquierda)
                Text(
                    text = String.format("%02d:00", hour),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .width(TIME_COLUMN_WIDTH)
                        .padding(top = topOffset + 4.dp, end = 8.dp), // Ajuste visual
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )

                // Línea divisoria
                HorizontalDivider(
                    modifier = Modifier
                        .padding(start = TIME_COLUMN_WIDTH, top = topOffset)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            }

            // 2. BLOQUES DE TAREAS (EVENTS)
            // Aquí ocurre la magia del posicionamiento absoluto
            tasks.forEach { task ->
                // Solo mostramos tareas que NO son "Todo el día" en la línea de tiempo
                if (!task.isAllDay) {
                    val (topDp, heightDp) = calculateTaskPosition(task.startTime, task.endTime)

                    TaskBlock(
                        task = task,
                        modifier = Modifier
                            .padding(start = TIME_COLUMN_WIDTH + 4.dp, end = 4.dp) // Margen derecho
                            .fillMaxWidth() // Por ahora ocupan todo el ancho (superpuestas si coinciden)
                            .offset(y = topDp)
                            .height(heightDp)
                            .clickable { onTaskClick(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskBlock(task: Task, modifier: Modifier = Modifier) {
    val bgColor = try { Color(android.graphics.Color.parseColor(task.colorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
    val textColor = getContrastColor(task.colorHex)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor.copy(alpha = 0.9f))
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(4.dp)
    ) {
        Text(
            text = task.title,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor,
            maxLines = 1
        )
        task.description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = textColor.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

// LÓGICA MATEMÁTICA
// Calcula dónde empieza (offset Y) y qué alto tiene la tarea
fun calculateTaskPosition(start: LocalDateTime, end: LocalDateTime): Pair<Dp, Dp> {
    val startMinutes = start.hour * 60 + start.minute
    val endMinutes = end.hour * 60 + end.minute

    // Duración en minutos (mínimo 15 minutos visuales para que se vea el bloque)
    val duration = (endMinutes - startMinutes).coerceAtLeast(15)

    // Regla de 3: Si 60min = HOUR_HEIGHT, entonces Xmin = ?
    val topOffset = HOUR_HEIGHT * (startMinutes / 60f)
    val height = HOUR_HEIGHT * (duration / 60f)

    return topOffset to height
}