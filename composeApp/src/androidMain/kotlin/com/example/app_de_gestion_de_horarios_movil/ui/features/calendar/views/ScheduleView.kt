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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// 1. OPTIMIZACIÓN: Formateadores estáticos (fuera de la recomposición)
// Se crean una sola vez para toda la vida de la app.
@RequiresApi(Build.VERSION_CODES.O)
private val TIME_FORMATTER_12H = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
@RequiresApi(Build.VERSION_CODES.O)
private val DAY_OF_WEEK_FORMATTER = DateTimeFormatter.ofPattern("EEE", Locale.getDefault())
@RequiresApi(Build.VERSION_CODES.O)
private val MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM", Locale.getDefault())

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScheduleView(
    tasksMap: Map<LocalDate, List<Task>>,
    onTaskClick: (Task) -> Unit
) {
    // 2. OPTIMIZACIÓN: Pre-cálculo de la lista (Estado Derivado)
    // Transformamos el Mapa en una Lista ordenada lista para renderizar.
    // Esto solo se ejecuta si 'tasksMap' cambia, no cada vez que haces scroll.
    val scheduleData = remember(tasksMap) {
        tasksMap.keys.sorted().map { date ->
            val tasks = tasksMap[date]?.sortedBy { it.startTime } ?: emptyList()
            ScheduleDayUiModel(date, tasks)
        }.filter { it.tasks.isNotEmpty() } // Filtramos días vacíos aquí para no procesarlos después
    }

    if (scheduleData.isEmpty()) {
        EmptyScheduleState()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            scheduleData.forEach { dayModel ->
                // Sticky Header
                // CORRECCIÓN AQUÍ: Usamos .toString() para la key
                stickyHeader(key = dayModel.date.toString()) {
                    ScheduleDayHeader(date = dayModel.date)
                }

                // Items
                items(
                    items = dayModel.tasks,
                    // task.id suele ser String o Long, así que esto está bien
                    key = { task -> task.id }
                ) { task ->
                    ScheduleTaskItem(
                        task = task,
                        onClick = { onTaskClick(task) }
                    )
                }
            }
        
        }
    }
}

// Modelo de datos interno para la UI (facilita el renderizado)
private data class ScheduleDayUiModel(
    val date: LocalDate,
    val tasks: List<Task>
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScheduleDayHeader(date: LocalDate) {
    val javaDate = remember(date) { date.toJavaLocalDate() }

    // Usamos el reloj del sistema de Kotlinx para la comparación
    val isToday = remember(date) {
        date == kotlinx.datetime.Clock.System.now()
            .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Columna de Día
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(50.dp)
        ) {
            Text(
                text = javaDate.format(DAY_OF_WEEK_FORMATTER).uppercase(),
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

        // Mes
        Text(
            text = javaDate.format(MONTH_FORMATTER).replaceFirstChar { it.titlecase() },
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
    // 4. OPTIMIZACIÓN: Cálculo de strings y colores solo cuando la tarea cambia
    val startTimeStr = remember(task.startTime) {
        task.startTime.toJavaLocalDateTime().format(TIME_FORMATTER_12H)
    }
    val endTimeStr = remember(task.endTime) {
        task.endTime.toJavaLocalDateTime().format(TIME_FORMATTER_12H)
    }

    // Parseo de color seguro y memoizado
    val taskColor = remember(task.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(task.colorHex))
        } catch (e: Exception) {
            Color.Gray // Color fallback seguro en lugar de depender del tema dinámicamente aquí
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(IntrinsicSize.Min)
    ) {
        // Hora
        Column(
            modifier = Modifier
                .width(65.dp)
                .padding(top = 4.dp),
            horizontalAlignment = Alignment.End
        ) {
            if (!task.isAllDay) {
                Text(
                    text = startTimeStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = endTimeStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            } else {
                Text(
                    text = "Todo\ndía",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    lineHeight = 12.sp // Ajuste fino para que "Todo día" se vea compacto
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Línea vertical
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(taskColor)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Contenido
        Column(
            modifier = Modifier
                .weight(1f)
                .background(taskColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!task.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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
            text = "No hay eventos programados",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}