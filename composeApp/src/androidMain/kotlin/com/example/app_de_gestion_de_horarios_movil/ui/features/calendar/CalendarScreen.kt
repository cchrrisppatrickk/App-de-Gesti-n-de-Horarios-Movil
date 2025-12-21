package com.example.app_de_gestion_de_horarios_movil.ui.features.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import kotlinx.datetime.*
import org.koin.androidx.compose.koinViewModel
import java.time.format.TextStyle // Solo para formatear nombres de mes (Java Time es mejor para Locales)
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // 1. Cabecera del Mes (< Octubre 2025 >)
        CalendarHeader(
            currentMonth = state.currentMonth,
            onPrevMonth = viewModel::onPreviousMonth,
            onNextMonth = viewModel::onNextMonth
        )

        // 2. Nombres de días (L M X J V S D)
        DaysOfWeekHeader()

        // 3. Cuadrícula del Calendario
        CalendarGrid(
            currentMonth = state.currentMonth,
            selectedDate = state.selectedDate,
            tasksMap = state.tasks,
            onDateSelected = viewModel::onDateSelected
        )

        Spacer(modifier = Modifier.height(16.dp))

        // AQUÍ IRÁ LA AGENDA (Lista de tareas del día seleccionado)
        // Por ahora un placeholder:
        Text(
            text = "Tareas del ${state.selectedDate}: ${state.tasks[state.selectedDate]?.size ?: 0}",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarHeader(
    currentMonth: LocalDate,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevMonth) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Mes anterior")
        }

        // Formato: "Octubre 2025"
        // Usamos java.time.Month para obtener el nombre traducido fácilmente
        val monthName = java.time.Month.of(currentMonth.monthNumber)
            .getDisplayName(TextStyle.FULL, Locale.getDefault())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

        Text(
            text = "$monthName ${currentMonth.year}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Mes siguiente")
        }
    }
}

@Composable
fun DaysOfWeekHeader() {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        val days = listOf("L", "M", "M", "J", "V", "S", "D")
        days.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarGrid(
    currentMonth: LocalDate,
    selectedDate: LocalDate,
    tasksMap: Map<LocalDate, List<Task>>,
    onDateSelected: (LocalDate) -> Unit
) {
    // Lógica para calcular días
    val daysInMonth = currentMonth.month.length(currentMonth.year % 4 == 0 && (currentMonth.year % 100 != 0 || currentMonth.year % 400 == 0)) // Simplificación de bisiesto o usar DatePeriod
    // Nota: kotlinx-datetime no tiene .length() directo en Month sin año, mejor calcularlo así:
    val nextMonth = currentMonth.plus(DatePeriod(months = 1))
    val daysCount = currentMonth.until(nextMonth, DateTimeUnit.DAY)

    // Calcular en qué día de la semana cae el día 1 (1 = Lunes, 7 = Domingo)
    val firstDayOfWeek = currentMonth.dayOfWeek.isoDayNumber
    val emptySlots = firstDayOfWeek - 1 // Si empieza lunes (1), 0 huecos.

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
    ) {
        // 1. Espacios vacíos al inicio
        items(emptySlots) {
            Box(modifier = Modifier.size(40.dp))
        }

        // 2. Días del mes
        items(daysCount) { dayOffset ->
            val date = currentMonth.plus(DatePeriod(days = dayOffset))
            val isSelected = date == selectedDate
            val isToday = date == Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val hasTasks = tasksMap[date]?.isNotEmpty() == true
            val taskColors = tasksMap[date]?.take(3)?.map { it.colorHex } ?: emptyList()

            DayCell(
                day = date.dayOfMonth,
                isSelected = isSelected,
                isToday = isToday,
                taskColors = taskColors,
                onClick = { onDateSelected(date) }
            )
        }
    }
}

@Composable
fun DayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    taskColors: List<String>,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else if (isToday) MaterialTheme.colorScheme.surfaceVariant
                else Color.Transparent
            )
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = day.toString(),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
        )

        // Indicadores de tareas (puntitos)
        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            taskColors.forEach { colorHex ->
                // Parseo básico de color (Deberías usar tu lógica de conversión existente)
                val color = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { Color.Gray }
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}