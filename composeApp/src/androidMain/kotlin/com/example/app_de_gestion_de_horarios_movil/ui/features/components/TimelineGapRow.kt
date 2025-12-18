package com.example.app_de_gestion_de_horarios_movil.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimelineGapRow(
    startTime: LocalDateTime, // Viene como java.time.LocalDateTime desde GapItem
    durationMinutes: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. LOGICA DE TIEMPO REAL (Igual que en TimelineTaskRow)
    // Actualización cada 15 segundos
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            delay(15_000L)
        }
    }

    // Formateadores
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val formattedTime = remember(startTime) { startTime.format(timeFormatter) }
    val currentTimeString = remember(currentTime) { currentTime.format(timeFormatter) }

    // 2. CALCULO DE PROGRESO PARA EL HUECO
    val progress = remember(startTime, durationMinutes, currentTime) {
        val end = startTime.plusMinutes(durationMinutes)

        when {
            currentTime.isAfter(end) -> 1.0f    // Hueco ya pasó
            currentTime.isBefore(startTime) -> 0.0f // Hueco futuro
            else -> {
                // Estamos DENTRO del tiempo libre
                val totalMinutes = durationMinutes.toFloat()
                val currentMinutes = ChronoUnit.MINUTES.between(startTime, currentTime).toFloat()
                if (totalMinutes > 0) (currentMinutes / totalMinutes).coerceIn(0f, 1f) else 0f
            }
        }
    }

    // 3. COLOR DEL HUECO ACTIVO
    // Usamos un Gris Medio (#9E9E9E).
    // - Si el progreso es 0, se verá gris muy claro (futureColor del Node).
    // - Si el progreso avanza, se verá este gris medio llenando la línea punteada.
    val activeGapColor = "#9E9E9E"

    // 4. ALTURA DINÁMICA
    val rowHeight = remember(durationMinutes) {
        TimelineConfig.calculateHeight(durationMinutes)
    }

    Row(
        modifier = modifier
            .height(rowHeight)
            .padding(vertical = 4.dp)
    ) {
        // COLUMNA 1: HORA
        Box(
            modifier = Modifier
                .width(50.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.labelMedium,
                // Si estamos en este hueco, resaltamos ligeramente la hora también en gris
                color = if (progress > 0f && progress < 1f) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        // COLUMNA 2: NODO Y LÍNEA (DINÁMICO)
        TimelineNode(
            colorHex = activeGapColor, // <--- Color del "relleno" del hueco
            isFirst = false,
            isLast = false,
            progress = progress,       // <--- Pasamos el progreso calculado
            currentTimeString = currentTimeString, // <--- Pasamos la hora flotante
            modifier = Modifier.fillMaxHeight()
        )

        // COLUMNA 3: LA TARJETA DE HUECO
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 16.dp, bottom = 4.dp)
        ) {
            GapCard(
                durationMinutes = durationMinutes,
                onClick = onClick,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}