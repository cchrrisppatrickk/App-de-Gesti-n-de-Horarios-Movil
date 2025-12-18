package com.example.app_de_gestion_de_horarios_movil.ui.components

import android.graphics.Color.parseColor
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TaskCard(
    task: Task,
    isVisualymediumOrLarge: Boolean,
    onToggleCompletion: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. LÓGICA DE COLOR
    val baseColor = remember(task.colorHex, task.isCompleted) {
        if (task.isCompleted) {
            Color.Gray
        } else {
            try {
                Color(parseColor(task.colorHex))
            } catch (e: Exception) {
                Color.Gray
            }
        }
    }

    // 2. FORMATEO DE HORA Y DURACIÓN (ACTUALIZADO A 12 HORAS)
    val timeInfoText = remember(task.startTime, task.endTime, task.durationMinutes) {
        // CAMBIO AQUÍ: "hh:mm a" para formato 02:30 PM
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")

        val start = task.startTime.toJavaLocalDateTime().format(formatter)
        val end = task.endTime.toJavaLocalDateTime().format(formatter)

        val hours = task.durationMinutes / 60
        val mins = task.durationMinutes % 60
        val durationString = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

        "$start - $end ($durationString)"
    }

    val textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
    val contentAlpha = if (task.isCompleted) 0.6f else 1f

    // Asegúrate de tener TaskIcons importado o definido en ui.components
    val taskIcon = remember(task.iconId) { TaskIcons.getIconById(task.iconId) }

    // 3. RENDERIZADO
    if (isVisualymediumOrLarge) {
        BlockTaskCard(
            task = task,
            color = baseColor,
            icon = taskIcon,
            textDecoration = textDecoration,
            alpha = contentAlpha,
            timeText = timeInfoText,
            onToggleCompletion = onToggleCompletion,
            modifier = modifier
        )
    } else {
        ShortTaskRow(
            task = task,
            color = baseColor,
            icon = taskIcon,
            textDecoration = textDecoration,
            alpha = contentAlpha,
            timeText = timeInfoText,
            onToggleCompletion = onToggleCompletion,
            modifier = modifier
        )
    }
}

@Composable
fun ShortTaskRow(
    task: Task,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    textDecoration: TextDecoration?,
    alpha: Float,
    timeText: String,
    onToggleCompletion: () -> Unit,
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .alpha(alpha),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón de completar
        TaskCheckButton(
            isCompleted = task.isCompleted,
            color = color,
            onClick = onToggleCompletion
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Icono pequeño
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Título y Hora en una línea (si cabe) o texto simple
        Column {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (textDecoration != null) color else MaterialTheme.colorScheme.onSurface,
                textDecoration = textDecoration,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // En vista corta, mostramos la hora muy pequeña debajo
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f),
                fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.9
            )
        }
    }
}

@Composable
fun BlockTaskCard(
    task: Task,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    textDecoration: TextDecoration?,
    alpha: Float,
    timeText: String,
    onToggleCompletion: () -> Unit,
    modifier: Modifier
) {
    Card(
        modifier = modifier.alpha(alpha),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.12f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize()
        ) {
            // Línea de acento lateral
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Columna Principal
            Column(modifier = Modifier.weight(1f)) {
                // Fila Superior: Checkbox + Título
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TaskCheckButton(
                        isCompleted = task.isCompleted,
                        color = color,
                        onClick = onToggleCompletion
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color.copy(alpha = 1f),
                        textDecoration = textDecoration,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Fila de Hora y Duración (Nueva)
                Text(
                    text = "⏰ $timeText",
                    style = MaterialTheme.typography.labelMedium,
                    color = color.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )

                // Descripción (si existe)
                if (!task.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = textDecoration,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Icono marca de agua
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color.copy(alpha = 0.25f),
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Bottom)
            )
        }
    }
}

// COMPONENTE VISUAL: Botón circular de check
@Composable
private fun TaskCheckButton(
    isCompleted: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (isCompleted) color else Color.Transparent)
            .border(
                width = 2.dp,
                color = if (isCompleted) color else color.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completada",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}