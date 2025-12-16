package com.example.app_de_gestion_de_horarios_movil.ui.components

import android.graphics.Color.parseColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TimelineNode(
    colorHex: String,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    val primaryColor = remember(colorHex) {
        try { Color(parseColor(colorHex)) } catch (e: Exception) { Color.Gray }
    }
    val lineColor = MaterialTheme.colorScheme.outlineVariant // Color gris suave para la línea

    Canvas(modifier = modifier.width(32.dp)) {
        val centerX = size.width / 2
        // Altura donde se dibuja el nodo (un poco más abajo del top para alinearse con el texto)
        val nodeCenterY = 20.dp.toPx()
        val circleRadius = 6.dp.toPx()
        val lineWidth = 2.dp.toPx()

        // 1. Línea Superior (conecta con el anterior)
        if (!isFirst) {
            drawLine(
                color = lineColor,
                start = Offset(centerX, 0f),
                end = Offset(centerX, nodeCenterY),
                strokeWidth = lineWidth
            )
        }

        // 2. Línea Inferior (conecta con el siguiente)
        if (!isLast) {
            drawLine(
                color = lineColor,
                start = Offset(centerX, nodeCenterY),
                end = Offset(centerX, size.height), // size.height va hasta el fondo del item
                strokeWidth = lineWidth
            )
        }

        // 3. El Nodo Central (Círculo)
        drawCircle(
            color = primaryColor,
            radius = circleRadius,
            center = Offset(centerX, nodeCenterY)
        )

        // Opcional: Dibujar un círculo blanco dentro para efecto de "anillo"
        drawCircle(
            color = Color.White,
            radius = circleRadius * 0.6f,
            center = Offset(centerX, nodeCenterY)
        )
    }
}