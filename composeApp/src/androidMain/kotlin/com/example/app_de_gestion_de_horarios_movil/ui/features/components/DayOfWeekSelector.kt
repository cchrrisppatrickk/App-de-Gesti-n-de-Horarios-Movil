package com.example.app_de_gestion_de_horarios_movil.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek

@Composable
fun DayOfWeekSelector(
    selectedDays: List<DayOfWeek>,
    onDaySelected: (DayOfWeek) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Iteramos los 7 días de la semana (Lunes a Domingo)
        DayOfWeek.values().forEach { day ->
            val isSelected = selectedDays.contains(day)

            // Texto corto: L, M, M, J, V, S, D
            // take(1) toma la primera letra. DayOfWeek.MONDAY.name es "MONDAY"
            val label = when(day) {
                DayOfWeek.MONDAY -> "L"
                DayOfWeek.TUESDAY -> "M"
                DayOfWeek.WEDNESDAY -> "X" // X para Miércoles para no confundir con Martes
                DayOfWeek.THURSDAY -> "J"
                DayOfWeek.FRIDAY -> "V"
                DayOfWeek.SATURDAY -> "S"
                DayOfWeek.SUNDAY -> "D"
                else -> "?"
            }

            DayToggle(
                label = label,
                isSelected = isSelected,
                onClick = { onDaySelected(day) }
            )
        }
    }
}

@Composable
private fun DayToggle(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = contentColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}