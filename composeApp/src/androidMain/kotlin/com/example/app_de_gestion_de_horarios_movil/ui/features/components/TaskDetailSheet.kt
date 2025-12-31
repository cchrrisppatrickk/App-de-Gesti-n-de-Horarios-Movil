package com.example.app_de_gestion_de_horarios_movil.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow // Importante para manejar el texto largo
import androidx.compose.ui.unit.dp
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.ui.components.TaskIcons
import com.example.app_de_gestion_de_horarios_movil.ui.components.toUiString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailSheet(
    task: Task,
    onDismissRequest: () -> Unit,
    onDelete: () -> Unit,
    onDeleteAll: () -> Unit,
    onEdit: () -> Unit,
    onEditAll: () -> Unit,
    onToggleComplete: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isRecurring = remember(task) { task.groupId != null }
    val scrollState = rememberScrollState() // Estado del scroll

    // --- COLORES DEL TEMA ---
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val errorColor = MaterialTheme.colorScheme.error
    val outlineColor = MaterialTheme.colorScheme.outline

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = surfaceColor,
        contentColor = onSurfaceColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                // CORRECCIÓN 1: Habilitar Scroll para todo el contenido
                .verticalScroll(scrollState)
                // Padding inferior extra grande para asegurar que los botones se vean al final
                .padding(bottom = 64.dp)
        ) {
            // ------------------------------------------------
            // 1. ENCABEZADO
            // ------------------------------------------------
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = remember(task.iconId) { TaskIcons.getIconById(task.iconId) }
                val categoryColor = remember(task.colorHex) {
                    try { Color(android.graphics.Color.parseColor(task.colorHex)) }
                    catch (e: Exception) { primaryColor }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        // CORRECCIÓN 2: Limitar líneas del título para evitar desbordamiento masivo
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isRecurring) {
                        Text(
                            text = "Tarea Recurrente",
                            style = MaterialTheme.typography.labelSmall,
                            color = primaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ------------------------------------------------
            // 2. INFORMACIÓN
            // ------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("HORARIO", style = MaterialTheme.typography.labelSmall, color = subTextColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${task.startTime.time.toUiString()} - ${task.endTime.time.toUiString()}",
                        style = MaterialTheme.typography.titleMedium,
                        color = onSurfaceColor
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("DURACIÓN", style = MaterialTheme.typography.labelSmall, color = subTextColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${task.durationMinutes} min",
                        style = MaterialTheme.typography.titleMedium,
                        color = onSurfaceColor
                    )
                }
            }

            if (!task.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = outlineColor.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(16.dp))

                Text("NOTAS", style = MaterialTheme.typography.labelSmall, color = subTextColor)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = onSurfaceColor.copy(alpha = 0.9f),
                    // Opcional: Limitar descripción también si es kilométrica
                    // maxLines = 10,
                    // overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ------------------------------------------------
            // 3. ACCIONES SECUNDARIAS (BOTONES)
            // ------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = errorColor,
                        containerColor = Color.Transparent
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, errorColor.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar")
                }

                OutlinedButton(
                    onClick = { if (isRecurring) showEditDialog = true else onEdit() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = subTextColor,
                        containerColor = Color.Transparent
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, subTextColor.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editar")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ------------------------------------------------
            // 4. BOTÓN PRINCIPAL
            // ------------------------------------------------
            Button(
                onClick = onToggleComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (task.isCompleted) outlineColor else primaryColor,
                    contentColor = if (task.isCompleted) Color.White else onPrimaryColor
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.Replay else Icons.Default.CheckCircle,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (task.isCompleted) "Reabrir Tarea" else "Finalizar Tarea",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // --- DIÁLOGOS (Sin cambios) ---
    if (showDeleteDialog) {
        BasicAlertDialog(
            onDismiss = { showDeleteDialog = false },
            title = if (isRecurring) "¿Borrar serie?" else "¿Eliminar tarea?",
            text = if (isRecurring) "¿Borrar solo esta o todas las futuras?" else "Esta acción es irreversible.",
            confirmText = if (isRecurring) "Todas" else "Eliminar",
            dismissText = if (isRecurring) "Solo esta" else "Cancelar",
            confirmColor = errorColor,
            onConfirm = { if (isRecurring) onDeleteAll() else onDelete() },
            onDismissAction = { if (isRecurring) onDelete() }
        )
    }

    if (showEditDialog) {
        BasicAlertDialog(
            onDismiss = { showEditDialog = false },
            title = "Editar serie recurrente",
            text = "¿Quieres aplicar los cambios a este evento o a toda la serie?",
            confirmText = "Todas",
            dismissText = "Solo esta",
            confirmColor = primaryColor,
            onConfirm = onEditAll,
            onDismissAction = onEdit
        )
    }
}

// Helper Composable para reutilizar el estilo de los diálogos
@Composable
fun BasicAlertDialog(
    onDismiss: () -> Unit,
    title: String,
    text: String,
    confirmText: String,
    dismissText: String,
    confirmColor: Color,
    onConfirm: () -> Unit,
    onDismissAction: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface, // #252525
        titleContentColor = MaterialTheme.colorScheme.onSurface, // #EEEEEE
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant, // #9E9E9E
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onConfirm()
            }) {
                Text(confirmText, color = confirmColor, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
                onDismissAction()
            }) {
                Text(dismissText, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}