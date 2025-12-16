package com.example.app_de_gestion_de_horarios_movil.ui.components

import android.graphics.Color.parseColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task

@Composable
fun TaskCard(
    task: Task,
    isVisualymediumOrLarge: Boolean, // Nuevo parámetro
    modifier: Modifier = Modifier
) {
    val baseColor = remember(task.colorHex) {
        try { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(task.colorHex)) }
        catch (e: Exception) { androidx.compose.ui.graphics.Color.Gray }
    }

    val taskIcon = remember(task.iconId) { TaskIcons.getIconById(task.iconId) }

    // Si es visualmente grande (>= 30 min), usamos el bloque
    if (isVisualymediumOrLarge) {
        BlockTaskCard(task, baseColor, taskIcon, modifier)
    } else {
        ShortTaskRow(task, baseColor, taskIcon, modifier)
    }
}

@Composable
fun ShortTaskRow(
    task: Task,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // Padding pequeño
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono flotante (Punto de anclaje)
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Título simple
        Text(
            text = task.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BlockTaskCard(
    task: Task,
    color: androidx.compose.ui.graphics.Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier
) {
    androidx.compose.material3.Card(
        modifier = modifier, // Llena el alto definido por TimelineTaskRow
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize()
        ) {
            // Línea de acento lateral (Decorativa)
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(color, androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Usamos Column con weight para empujar el contenido hacia arriba
            Column(modifier = Modifier.weight(1f)) {
                // Título
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color.copy(alpha = 1f),
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )

                // Mostrar duración real si es una tarea larga visualmente comprimida
                if (task.durationMinutes > 120) {
                    Text(
                        text = "⏳ ${task.durationMinutes / 60}h ${task.durationMinutes % 60}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = color.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                // Descripción (Solo si cabe)
                if (!task.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3, // Limitamos líneas para que no se desborde si comprimimos la tarjeta
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            // Icono (Alineado arriba a la derecha)
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color.copy(alpha = 0.3f),
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Top) // ¡Importante! Siempre arriba
            )
        }
    }
}