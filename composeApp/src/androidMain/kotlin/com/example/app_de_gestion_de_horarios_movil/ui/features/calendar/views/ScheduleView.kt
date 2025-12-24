package com.example.app_de_gestion_de_horarios_movil.ui.features.calendar.views

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.ui.features.calendar.getContrastColor
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScheduleView(
    tasksMap: Map<LocalDate, List<Task>>, // Recibe el mapa completo
    onTaskClick: (Task) -> Unit
) {
    // Ordenamos las fechas para que la lista sea cronológica
    val sortedDates = remember(tasksMap) { tasksMap.keys.sorted() }

    if (sortedDates.isEmpty()) {
        EmptyScheduleState()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp) // Espacio para el FAB
        ) {
            sortedDates.forEach { date ->
                val tasksForDay = tasksMap[date]?.sortedBy { it.startTime } ?: emptyList()

                if (tasksForDay.isNotEmpty()) {
                    // 1. CABECERA PEGAJOSA (STICKY HEADER)
                    stickyHeader {
                        ScheduleDayHeader(date = date)
                    }

                    // 2. ITEMS DE TAREA
                    items(tasksForDay) { task ->
                        ScheduleTaskItem(
                            task = task,
                            onClick = { onTaskClick(task) }
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleDayHeader(date: LocalDate) {
    // Formateadores de fecha
    val dayOfWeekFormatter = remember { DateTimeFormatter.ofPattern("EEE", Locale.getDefault()) }
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM", Locale.getDefault()) }

    val javaDate = date.toJavaLocalDate()
    val isToday = date == kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background) // Fondo opaco para tapar al hacer scroll
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Columna de Día (Lun 23)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(50.dp)
        ) {
            Text(
                text = javaDate.format(dayOfWeekFormatter).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Mes y Año (Diciembre 2025) - Opcional, o línea divisoria
        Text(
            text = javaDate.format(monthFormatter).replaceFirstChar { it.titlecase() },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(modifier = Modifier.width(100.dp))
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleTaskItem(
    task: Task,
    onClick: () -> Unit
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val startTime = task.startTime.toJavaLocalDateTime().format(timeFormatter)
    val endTime = task.endTime.toJavaLocalDateTime().format(timeFormatter)

    val taskColor = try { Color(android.graphics.Color.parseColor(task.colorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(IntrinsicSize.Min) // Para que la línea vertical mida lo mismo que el contenido
    ) {
        // 1. Hora (Columna Izquierda)
        Column(
            modifier = Modifier.width(50.dp).padding(top = 4.dp),
            horizontalAlignment = Alignment.End
        ) {
            if (!task.isAllDay) {
                Text(
                    text = startTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = endTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Todo\ndía",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 2. Línea de Color y Contenido
        // Barra vertical de color
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(taskColor)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Tarjeta o Texto del Evento
        Column(
            modifier = Modifier
                .weight(1f)
                .background(taskColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)) // Fondo sutil opcional
                .padding(8.dp)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!task.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun EmptyScheduleState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No hay eventos este mes",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}