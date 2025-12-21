package com.example.app_de_gestion_de_horarios_movil.ui.features.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.app_de_gestion_de_horarios_movil.domain.model.NotificationType
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.ui.components.TaskIcons
import kotlinx.datetime.toJavaLocalTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailSheet(
    task: Task, // Cambié el nombre a 'task' para ser consistentes (puede ser Evento o Tarea)
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleComplete: () -> Unit // Nuevo callback para finalizar
) {
    val eventColor = try { Color(android.graphics.Color.parseColor(task.colorHex)) } catch (e: Exception) { Color.Gray }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // --- 1. CABECERA CON ACCIONES ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }

                Row {
                    // Botón FINALIZAR / COMPLETAR
                    IconButton(onClick = onToggleComplete) {
                        Icon(
                            imageVector = if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                            contentDescription = "Finalizar",
                            tint = if (task.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Botón EDITAR
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }

                    // Botón ELIMINAR
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. TÍTULO E ICONO ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(eventColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = TaskIcons.getIconById(task.iconId),
                        contentDescription = null,
                        tint = eventColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        // Tachado si está completada (opcional visual)
                        textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    )
                    // Mostrar fecha en texto
                    Text(
                        text = task.startTime.date.toString(), // Puedes formatearlo mejor aquí
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 3. HORARIO ---
            DetailRow(
                icon = Icons.Default.AccessTime,
                title = "Horario",
                content = if (task.isAllDay) "Todo el día" else {
                    val fmt = DateTimeFormatter.ofPattern("hh:mm a")
                    "${task.startTime.time.toJavaLocalTime().format(fmt)} - ${task.endTime.time.toJavaLocalTime().format(fmt)}"
                }
            )

            // --- 4. RECORDATORIOS ---
            if (task.activeAlerts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                DetailRow(
                    icon = Icons.Default.Notifications,
                    title = "Recordatorios",
                    content = task.activeAlerts.joinToString(", ") { type ->
                        when(type) {
                            NotificationType.AT_START -> "Al inicio"
                            NotificationType.AT_END -> "Al finalizar"
                            NotificationType.FIFTEEN_MIN_BEFORE -> "15 min antes"
                            NotificationType.CUSTOM -> "Personalizado"
                        }
                    }
                )
            }

            // --- 5. DESCRIPCIÓN ---
            if (!task.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Helper Composable para filas (si no lo tienes ya en otro archivo)
@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, content: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = content, style = MaterialTheme.typography.bodyLarge)
        }
    }
}