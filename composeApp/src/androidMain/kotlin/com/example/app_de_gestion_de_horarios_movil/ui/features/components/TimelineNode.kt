package com.example.app_de_gestion_de_horarios_movil.ui.components

import android.graphics.Color.parseColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TimelineNode(
    colorHex: String,
    isFirst: Boolean,
    isLast: Boolean,
    progress: Float,
    currentTimeString: String, // <--- NUEVO: Recibe "14:33"
    modifier: Modifier = Modifier
) {
    val primaryColor = remember(colorHex) {
        try { Color(parseColor(colorHex)) } catch (e: Exception) { Color.Gray }
    }
    // Color suave para lo que no ha pasado
    val futureColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    // Herramienta para medir y dibujar texto en Canvas
    val textMeasurer = rememberTextMeasurer()

    // Estilo del texto flotante (pequeño y negrita)
    val textStyle = MaterialTheme.typography.labelSmall.copy(
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
    )

    // --- NUEVO: EFECTO DE LÍNEA DISCONTINUA ---
    // floatArrayOf(longitud_linea, longitud_espacio)
    val dashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f) }

    Canvas(modifier = modifier.width(45.dp)) { // Aumenté un poco el ancho para que quepa el texto
        val centerX = size.width / 2
        val nodeCenterY = 20.dp.toPx()
        val circleRadius = 6.dp.toPx()
        val lineWidth = 3.dp.toPx()

        // 1. LÍNEA SUPERIOR
        if (!isFirst) {
            drawLine(
                color = if (progress > 0f) primaryColor else futureColor,
                start = Offset(centerX, 0f),
                end = Offset(centerX, nodeCenterY),
                strokeWidth = lineWidth,
                pathEffect = dashEffect // <--- APLICAMOS GUIONES
            )
        }

        // 2. LÍNEA INFERIOR (Barra de Progreso)
        if (!isLast) {
            val lineStart = nodeCenterY
            val lineEnd = size.height
            val totalLength = lineEnd - lineStart

            val fillHeight = totalLength * progress
            val currentY = lineStart + fillHeight

            // A. Parte Coloreada (Pasado)
            if (progress > 0f) {
                drawLine(
                    color = primaryColor,
                    start = Offset(centerX, lineStart),
                    end = Offset(centerX, currentY),
                    strokeWidth = lineWidth,
                    pathEffect = dashEffect // <--- APLICAMOS GUIONES
                )
            }

            // B. Parte Gris (Futuro)
            if (progress < 1f) {
                drawLine(
                    color = futureColor,
                    start = Offset(centerX, currentY),
                    end = Offset(centerX, lineEnd),
                    strokeWidth = lineWidth,
                    pathEffect = dashEffect // <--- APLICAMOS GUIONES
                )
            }

            // C. INDICADOR "AHORA" CON TEXTO
            if (progress > 0f && progress < 1f) {
                // El punto brillante
                drawCircle(
                    color = primaryColor,
                    radius = lineWidth * 2f,
                    center = Offset(centerX, currentY)
                )
                drawCircle(
                    color = Color.White,
                    radius = lineWidth * 0.8f,
                    center = Offset(centerX, currentY)
                )

                // --- DIBUJAR LA HORA FLOTANTE ---
                val textLayoutResult = textMeasurer.measure(
                    text = currentTimeString,
                    style = textStyle
                )

                // Calculamos posición: A la izquierda del punto
                // (Para que no tape la tarjeta de la derecha)
                val textWidth = textLayoutResult.size.width
                val textHeight = textLayoutResult.size.height

                // Fondo del texto (Bocadillo)
                val padding = 8f
                val boxWidth = textWidth + padding * 2
                val boxHeight = textHeight + padding * 2

                // Posición: Un poco a la izquierda (-12dp) y centrado verticalmente con el punto
                val boxLeft = centerX - boxWidth - 12.dp.toPx()
                val boxTop = currentY - (boxHeight / 2)

                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(boxLeft, boxTop),
                    size = Size(boxWidth, boxHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )

                // El Texto
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(boxLeft + padding, boxTop + padding)
                )
            }
        }

        // 3. NODO PRINCIPAL
        val isActiveOrPast = progress > 0f
        drawCircle(
            color = if (isActiveOrPast) primaryColor else futureColor,
            radius = circleRadius,
            center = Offset(centerX, nodeCenterY)
        )
        drawCircle(
            color = Color.White,
            radius = circleRadius * 0.6f,
            center = Offset(centerX, nodeCenterY)
        )
    }
}