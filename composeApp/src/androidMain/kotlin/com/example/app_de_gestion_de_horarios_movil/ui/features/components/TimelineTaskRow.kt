package com.example.app_de_gestion_de_horarios.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.app_de_gestion_de_horarios.domain.model.Task
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimelineTaskRow(
    task: Task,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    // Formateador de hora (Java Time estándar)
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val formattedTime = remember(task.startTime) {
        task.startTime.toJavaLocalDateTime().format(timeFormatter)
    }

    Row(
        // IMPORTANTE: Fuerza a que la línea vertical se estire a la altura de la tarjeta
        modifier = modifier
            .height(IntrinsicSize.Min)
            .padding(vertical = 4.dp)
    ) {
        // COLUMNA 1: Hora
        Text(
            text = formattedTime,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            // Ajuste fino para alinear visualmente con el nodo
            modifier = Modifier
                .width(50.dp)
                .padding(top = 12.dp, start = 8.dp)
        )

        // COLUMNA 2: Nodo y Línea (Se estira en altura)
        TimelineNode(
            colorHex = task.colorHex,
            isFirst = isFirst,
            isLast = isLast,
            modifier = Modifier.fillMaxHeight()
        )

        // COLUMNA 3: Tarjeta de Tarea (Ocupa el resto del ancho)
        TaskCard(
            task = task,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, end = 16.dp, bottom = 12.dp)
        )
    }
}