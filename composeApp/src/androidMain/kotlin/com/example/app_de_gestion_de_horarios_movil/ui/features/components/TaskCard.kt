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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task

@Composable
fun TaskCard(
    task: Task,
    isVisualymediumOrLarge: Boolean,
    modifier: Modifier = Modifier
) {
    // 1. LÓGICA DE COLOR
    // Si está completada, usamos Gris Oscuro (para texto) y Gris Claro (para fondos) implícitamente
    val baseColor = remember(task.colorHex, task.isCompleted) {
        if (task.isCompleted) {
            Color.Gray // Usamos gris para todo el tema
        } else {
            try {
                Color(parseColor(task.colorHex))
            } catch (e: Exception) {
                Color.Gray
            }
        }
    }

    // 2. ESTILOS DE ESTADO (Tachado y Opacidad)
    val textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
    // Si está completa, reducimos la opacidad general al 60%
    val contentAlpha = if (task.isCompleted) 0.6f else 1f

    val taskIcon = remember(task.iconId) { TaskIcons.getIconById(task.iconId) }

    // 3. RENDERIZADO
    // Pasamos los nuevos estilos a los sub-componentes
    if (isVisualymediumOrLarge) {
        BlockTaskCard(
            task = task,
            color = baseColor,
            icon = taskIcon,
            textDecoration = textDecoration,
            alpha = contentAlpha,
            modifier = modifier
        )
    } else {
        ShortTaskRow(
            task = task,
            color = baseColor,
            icon = taskIcon,
            textDecoration = textDecoration,
            alpha = contentAlpha,
            modifier = modifier
        )
    }
}

@Composable
fun ShortTaskRow(
    task: Task,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    textDecoration: TextDecoration?, // Recibe el tachado
    alpha: Float,                    // Recibe la opacidad
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .alpha(alpha), // APLICAMOS LA OPACIDAD AQUÍ
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono flotante
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
            color = if (textDecoration != null) color else MaterialTheme.colorScheme.onSurface, // Si está completa, usa el gris base
            textDecoration = textDecoration, // APLICAMOS TACHADO
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BlockTaskCard(
    task: Task,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    textDecoration: TextDecoration?, // Recibe el tachado
    alpha: Float,                    // Recibe la opacidad
    modifier: Modifier
) {
    Card(
        modifier = modifier.alpha(alpha), // APLICAMOS LA OPACIDAD A TODA LA TARJETA
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            // Fondo muy sutil basado en el color (gris si está completa)
            containerColor = color.copy(alpha = 0.15f)
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

            Column(modifier = Modifier.weight(1f)) {
                // Título
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color.copy(alpha = 1f), // Texto sólido en el color base
                    textDecoration = textDecoration, // APLICAMOS TACHADO
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Duración (para tareas comprimidas visualmente)
                if (task.durationMinutes > 120) {
                    Text(
                        text = "⏳ ${task.durationMinutes / 60}h ${task.durationMinutes % 60}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = color.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                // Descripción
                if (!task.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = textDecoration, // APLICAMOS TACHADO TAMBIÉN AQUÍ (Opcional)
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Icono marca de agua
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color.copy(alpha = 0.3f),
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Top)
            )
        }
    }
}