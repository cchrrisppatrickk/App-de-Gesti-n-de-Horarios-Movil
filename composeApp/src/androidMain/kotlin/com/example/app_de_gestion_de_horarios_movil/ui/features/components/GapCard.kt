package com.example.app_de_gestion_de_horarios_movil.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.FreeBreakfast
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GapCard(
    durationMinutes: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Usamos MaterialTheme para los colores, pero en tono "desactivado"
    val contentColor = MaterialTheme.colorScheme.secondary
    val backgroundColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .fillMaxSize() // Ocupa todo el espacio que le pase la fila padre
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        // 1. FONDO Y BORDE (Custom Drawing para borde punteado)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
            )
            drawRoundRect(
                color = contentColor.copy(alpha = 0.3f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()),
                style = stroke
            )
        }

        // 2. CONTENIDO (Idéntico layout que una TaskCard simple)
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono sutil
            Icon(
                imageVector = if (durationMinutes >= 45) Icons.Outlined.FreeBreakfast else Icons.Outlined.AddCircleOutline,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = "Espacio Libre",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor.copy(alpha = 0.8f)
                )
                Text(
                    text = "Tienes ${durationMinutes} min disponibles",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}