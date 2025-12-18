package com.example.app_de_gestion_de_horarios_movil.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onDelete: () -> Unit, // Borrar Individual
    onDeleteAll: () -> Unit, // <--- NUEVO CALLBACK: Borrar Todo el Grupo
    onEdit: () -> Unit,
    onToggleComplete: () -> Unit
) {

    // 1. ESTADO LOCAL PARA CONTROLAR EL DIÁLOGO
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Detectamos si es recurrente (tiene groupId)
    val isRecurring = remember(task) { task.groupId != null }

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp) // Espacio para navbar
                .fillMaxWidth()
        ) {
            // --- ENCABEZADO: Icono y Título ---
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                        textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    )
                    // Indicador visual de recurrencia
                    if (isRecurring) {
                        Text(
                            text = "Tarea Recurrente",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
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
                    // EN LUGAR DE onDelete(), AHORA ACTIVAMOS EL DIÁLOGO
                    onClick = { showDeleteDialog = true },
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

    // 3. COMPONENTE DE ALERTA (Se renderiza sobre el Sheet si el estado es true)
    // --- DIÁLOGO INTELIGENTE ---
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = {
                Text(text = if (isRecurring) "¿Eliminar tarea recurrente?" else "¿Eliminar tarea?")
            },
            text = {
                Text(
                    text = if (isRecurring)
                        "Esta es una tarea repetitiva. ¿Quieres borrar solo esta instancia o toda la serie?"
                    else
                        "¿Estás seguro? Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                // Si es recurrente, este botón es "Eliminar TODAS"
                if (isRecurring) {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            onDeleteAll() // Llama a borrar grupo
                        }
                    ) {
                        Text("Borrar todas", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Si es normal, este es el botón de confirmar normal
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            onDelete()
                        }
                    ) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                // Si es recurrente, necesitamos un botón extra para "Solo esta"
                if (isRecurring) {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            onDelete() // Llama a borrar solo esta
                        }
                    ) {
                        Text("Solo esta")
                    }
                } else {
                    // Botón Cancelar normal
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            }
        )
    }

}
