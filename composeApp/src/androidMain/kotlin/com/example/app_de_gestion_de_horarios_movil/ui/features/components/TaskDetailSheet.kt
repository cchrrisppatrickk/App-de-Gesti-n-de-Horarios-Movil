package com.example.app_de_gestion_de_horarios_movil.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.ui.components.TaskIcons
import com.example.app_de_gestion_de_horarios_movil.ui.features.create_task.toUiString // Reusamos la extension

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailSheet(
    task: Task,
    onDismissRequest: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onToggleComplete: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp) // Espacio para navbar
                .fillMaxWidth()
        ) {
            // --- ENCABEZADO: Icono y Título ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icono grande
                val icon = remember(task.iconId) { TaskIcons.getIconById(task.iconId) }
                val color = remember(task.colorHex) {
                    try { Color(android.graphics.Color.parseColor(task.colorHex)) }
                    catch (e: Exception) { Color.Gray }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        // Si está completada, tachamos visualmente (opcional)
                        textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    )

                    // Estado visual
                    if (task.isCompleted) {
                        Text(
                            text = "COMPLETADA",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- INFO TEMPORAL ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Horario", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(
                        text = "${task.startTime.time.toUiString()} - ${task.endTime.time.toUiString()}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Duración", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(
                        text = "${task.durationMinutes} min",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            if (!task.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Notas", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(text = task.description, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- BOTONES DE ACCIÓN ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. ELIMINAR
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar")
                }

                // 2. EDITAR
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editar")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. FINALIZAR / REABRIR (Botón Principal)
            Button(
                onClick = onToggleComplete,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (task.isCompleted) Color.Gray else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.Replay else Icons.Default.CheckCircle,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (task.isCompleted) "Reabrir Tarea" else "Finalizar Tarea")
            }
        }
    }
}