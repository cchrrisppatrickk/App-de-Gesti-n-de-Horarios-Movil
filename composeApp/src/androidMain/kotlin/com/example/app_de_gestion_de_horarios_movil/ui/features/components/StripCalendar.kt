package com.example.app_de_gestion_de_horarios_movil.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

@Composable
fun StripCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Generar rango de fechas (Ej: 2 semanas atrás y 2 semanas adelante)
    // En una app real, esto podría venir paginado del ViewModel
    val dates = remember {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val start = today.minus(DatePeriod(days = 14))
        val end = today.plus(DatePeriod(days = 14))

        val dateList = mutableListOf<LocalDate>()
        var current = start
        while (current <= end) {
            dateList.add(current)
            current = current.plus(DatePeriod(days = 1))
        }
        dateList
    }

    // Estado del scroll para centrar la selección
    val listState = rememberLazyListState()

    // Efecto: Cuando cambia la fecha seleccionada, scrollear hacia ella
    LaunchedEffect(selectedDate) {
        val index = dates.indexOf(selectedDate)
        if (index >= 0) {
            listState.animateScrollToItem(index, scrollOffset = -150) // Offset para centrar aprox
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp, // Sombra suave bajo el calendario
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            // Mes y Año (Ej: "Diciembre 2025")
            Text(
                text = "${selectedDate.month.name.lowercase().capitalize()} ${selectedDate.year}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dates) { date ->
                    CalendarDayItem(
                        date = date,
                        isSelected = date == selectedDate,
                        // Aquí conectaríamos con la DB para saber si hay tareas ese día.
                        // Por ahora simulamos que los días pares tienen carga.
                        hasEvents = date.dayOfMonth % 2 == 0,
                        onClick = { onDateSelected(date) }
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarDayItem(
    date: LocalDate,
    isSelected: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit
) {
    // Animación de colores
    val backgroundColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    )
    val contentColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    )
    val borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(50.dp)
            .clip(RoundedCornerShape(12.dp)) // Forma de "Pastilla" vertical
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        // Día de la semana (L, M, X...)
        Text(
            text = date.dayOfWeek.name.take(1), // Primera letra
            style = MaterialTheme.typography.labelMedium,
            color = contentColor.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Número del día (25)
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Indicador de Carga (Puntito)
        if (hasEvents) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
            )
        } else {
            // Espaciador invisible para mantener la altura constante
            Spacer(modifier = Modifier.size(4.dp))
        }
    }
}

// Función auxiliar para capitalizar meses (String extension)
fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}