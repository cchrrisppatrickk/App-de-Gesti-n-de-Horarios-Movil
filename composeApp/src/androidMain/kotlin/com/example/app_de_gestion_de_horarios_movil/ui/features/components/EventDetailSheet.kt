package com.example.app_de_gestion_de_horarios_movil.ui.features.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Repeat
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.ui.components.TaskIcons
import com.example.app_de_gestion_de_horarios_movil.ui.components.toUiString
import com.example.app_de_gestion_de_horarios_movil.ui.features.home.BasicAlertDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailSheet(
    task: Task,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onDeleteAll: () -> Unit,
    onEdit: () -> Unit,
    onEditAll: () -> Unit
) {
    // ESTADOS PARA LOS DIÁLOGOS
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isRecurring = remember(task) { task.groupId != null }
    val scrollState = rememberScrollState() // Estado para el scroll

    // Colores
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val subTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val errorColor = MaterialTheme.colorScheme.error
    val outlineColor = MaterialTheme.colorScheme.outline

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = surfaceColor,
        contentColor = onSurfaceColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth() // Asegurar ancho completo
                .padding(horizontal = 24.dp)
                // CORRECCIÓN 1: Habilitar scroll para textos largos
                .verticalScroll(scrollState)
                // Padding inferior grande para que los botones no queden pegados al borde
                .padding(bottom = 64.dp)
        ) {
            // 1. ENCABEZADO
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = remember(task.iconId) {
                    try { TaskIcons.getIconById(task.iconId) } catch (e: Exception) { Icons.Default.Event }
                }
                val categoryColor = remember(task.colorHex) {
                    try { Color(android.graphics.Color.parseColor(task.colorHex)) } catch (e: Exception) { primaryColor }
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

                Column(modifier = Modifier.weight(1f)) { // weight para respetar límites
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor,
                        // CORRECCIÓN 2: Limitar líneas del título
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isRecurring) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Repeat,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Evento Recurrente",
                                style = MaterialTheme.typography.labelSmall,
                                color = primaryColor
                            )
                        }
                    } else {
                        Text(
                            text = "Evento",
                            style = MaterialTheme.typography.labelSmall,
                            color = subTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. INFORMACIÓN
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
                    color = onSurfaceColor.copy(alpha = 0.9f)
                    // Nota: Aquí se permite scroll natural gracias al Column.verticalScroll
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 3. ACCIONES PRINCIPALES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ELIMINAR
                Button(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = errorColor.copy(alpha = 0.1f),
                        contentColor = errorColor
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar")
                }

                // EDITAR
                Button(
                    onClick = { if (isRecurring) showEditDialog = true else onEdit() },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editar")
                }
            }
        }
    }

    // DIÁLOGOS
    if (showDeleteDialog) {
        BasicAlertDialog(
            onDismiss = { showDeleteDialog = false },
            title = if (isRecurring) "¿Borrar serie?" else "¿Eliminar evento?",
            text = if (isRecurring) "Este evento se repite. ¿Quieres borrar solo este día o toda la serie?" else "Esta acción no se puede deshacer.",
            confirmText = if (isRecurring) "Toda la serie" else "Eliminar",
            dismissText = if (isRecurring) "Solo hoy" else "Cancelar",
            confirmColor = errorColor,
            onConfirm = { if (isRecurring) onDeleteAll() else onDelete() },
            onDismissAction = { if (isRecurring) onDelete() }
        )
    }

    if (showEditDialog) {
        BasicAlertDialog(
            onDismiss = { showEditDialog = false },
            title = "Editar evento recurrente",
            text = "¿Quieres aplicar los cambios solo a este día o a todos los eventos de la serie?",
            confirmText = "Toda la serie",
            dismissText = "Solo hoy",
            confirmColor = primaryColor,
            onConfirm = onEditAll,
            onDismissAction = onEdit
        )
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