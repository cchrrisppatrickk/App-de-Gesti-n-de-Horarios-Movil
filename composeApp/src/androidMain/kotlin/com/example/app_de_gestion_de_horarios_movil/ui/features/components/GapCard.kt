package com.example.app_de_gestion_de_horarios_movil.ui.features.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.outlined.FreeBreakfast
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GapCard(
    durationMinutes: Long,
    startTime: java.time.LocalDateTime,
    endTime: java.time.LocalDateTime,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Formato de hora (ej: 15:30 - 16:00)
    val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
    val rangeText = "${startTime.format(timeFormatter)} - ${endTime.format(timeFormatter)}"

    // Altura dinámica pero sutil
    val height = 60.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(vertical = 6.dp, horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp) // Borde redondeado sutil
            )
            // Estilo de línea punteada sería ideal con pathEffect, pero un borde suave funciona bien
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (durationMinutes >= 45) Icons.Outlined.FreeBreakfast else Icons.Default.AddCircleOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Tiempo Libre (${durationMinutes}m)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Planificar descanso o tarea ($rangeText)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}