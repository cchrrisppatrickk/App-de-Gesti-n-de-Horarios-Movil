package com.example.app_de_gestion_de_horarios_movil.ui.features.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ChatBubbleOutline
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
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {} // No se usa en top level, pero se mantiene por firma
) {
    val state by viewModel.uiState.collectAsState()

    // Estructura principal con Scaffold para soportar el FAB y la TopBar
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CalendarTopBar(
                currentMonth = state.currentMonth,
                onMenuClick = { /* TODO: Abrir Drawer */ },
                onSearchClick = { /* TODO: Buscar */ },
                onMoreClick = { /* TODO: Opciones */ }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Crear Evento/Tarea */ },
                containerColor = Color(0xFF2196F3), // Azul estilo Google
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear evento")
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Cabecera de días (DOM LUN MAR...)
            DaysOfWeekHeader()

            // 2. Cuadrícula del Calendario (Ocupa todo el espacio restante)
            CalendarGrid(
                modifier = Modifier.weight(1f),
                currentMonth = state.currentMonth,
                selectedDate = state.selectedDate,
                tasksMap = state.tasks,
                onDateSelected = viewModel::onDateSelected,
                // Pasamos callbacks de cambio de mes para swipe (opcional futuro) o navegación
                onPrevMonth = viewModel::onPreviousMonth,
                onNextMonth = viewModel::onNextMonth
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarTopBar(
    currentMonth: LocalDate,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    // Formato: "dic 2025"
    val monthName = java.time.Month.of(currentMonth.monthNumber)
        .getDisplayName(TextStyle.SHORT, Locale.getDefault()) // SHORT para "dic"
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    val title = "$monthName ${currentMonth.year}"

    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 22.sp,
                fontWeight = FontWeight.Normal
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menú")
            }
        },
        actions = {
            // Botón "PRO" simulado (Chip)
            AssistChip(
                onClick = { },
                label = { Text("PRO", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Color(0xFFFFE0B2), // Naranja claro
                    labelColor = Color(0xFFE65100)
                ),
                border = null,
                modifier = Modifier.height(24.dp)
            )
            IconButton(onClick = { /* Chat */ }) {
                Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Chat")
            }
            IconButton(onClick = onMoreClick) {
                Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun DaysOfWeekHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Empezamos en Domingo para coincidir con la imagen (dom lun mar...)
        val days = listOf("dom", "lun", "mar", "mié", "jue", "vie", "sáb")
        days.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarGrid(
    modifier: Modifier = Modifier,
    currentMonth: LocalDate,
    selectedDate: LocalDate,
    tasksMap: Map<LocalDate, List<Task>>,
    onDateSelected: (LocalDate) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    // Lógica de fechas (usando DatePeriod de kotlinx-datetime)
    // Nota: Para que el calendario empiece en Domingo como la imagen, necesitamos ajustar la lógica.
    // IsoDayNumber: 1=Lunes ... 7=Domingo.
    // Si queremos empezar en Domingo, y el mes empieza en Lun(1), offset = 1. Si empieza Dom(7), offset = 0.

    val firstDayOfMonth = LocalDate(currentMonth.year, currentMonth.month, 1)
    val dayOfWeekIso = firstDayOfMonth.dayOfWeek.isoDayNumber

    // Cálculo de celdas vacías para inicio en Domingo:
    // Lunes (1) -> 1 hueco (Dom)
    // Martes (2) -> 2 huecos
    // Domingo (7) -> 0 huecos
    val emptySlots = if (dayOfWeekIso == 7) 0 else dayOfWeekIso

    val daysInMonth = currentMonth.month.length(currentMonth.year % 4 == 0 && (currentMonth.year % 100 != 0 || currentMonth.year % 400 == 0))

    // Líneas de la cuadrícula: Color gris muy claro
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = modifier
            .fillMaxWidth()
            // Borde superior de toda la grilla
            .border(BorderStroke(0.5.dp, gridColor)),
        contentPadding = PaddingValues(0.dp)
    ) {
        // 1. Espacios vacíos (Días del mes anterior)
        // Podríamos rellenar con números grises, pero por ahora dejaremos vacío o blancos
        items(emptySlots) {
            Box(
                modifier = Modifier
                    .aspectRatio(0.6f) // Relación de aspecto rectangular (alto)
                    .border(BorderStroke(0.25.dp, gridColor)) // Bordes internos
            )
        }

        // 2. Días del mes actual
        items(daysInMonth) { dayOffset ->
            val date = LocalDate(currentMonth.year, currentMonth.month, 1).plus(DatePeriod(days = dayOffset))
            val isToday = date == Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val hasTasks = tasksMap[date]?.isNotEmpty() == true

            // Colores de los puntos (limitado a 3 para no saturar)
            val taskColors = tasksMap[date]?.take(5)?.map { it.colorHex } ?: emptyList()

            DayCell(
                day = date.dayOfMonth,
                isToday = isToday,
                isSelected = date == selectedDate,
                taskColors = taskColors,
                gridColor = gridColor,
                onClick = { onDateSelected(date) }
            )
        }

        // Relleno final para completar la cuadrícula (opcional, mejora la estética)
        val totalCells = emptySlots + daysInMonth
        val remainingCells = (7 - (totalCells % 7)) % 7
        items(remainingCells) {
            Box(
                modifier = Modifier
                    .aspectRatio(0.6f)
                    .border(BorderStroke(0.25.dp, gridColor))
            )
        }
    }
}

@Composable
fun DayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean, // Mantenemos la lógica de selección aunque visualmente priorizamos "Today"
    taskColors: List<String>,
    gridColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            // Forzamos que sea alto (rectangular)
            .aspectRatio(0.6f) // 0.6 es más alto que ancho (aprox estilo captura)
            .border(BorderStroke(0.25.dp, gridColor)) // Líneas de división
            .clickable { onClick() }
            .padding(4.dp),
    ) {
        // 1. Número del día (Top Center o Top Left)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp)
                .size(28.dp) // Tamaño del círculo de selección
                .clip(CircleShape)
                .background(if (isToday) Color(0xFF2196F3) else Color.Transparent), // Azul si es hoy
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
        }

        // 2. Puntos de tareas (Debajo del número)
        // La imagen muestra barras de texto, pero usamos puntos por ahora como pediste
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter) // Alineados abajo o centro
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            taskColors.forEach { colorHex ->
                val color = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { Color.Gray }

//                // Opción A: Puntos (Dots)
//                Box(
//                    modifier = Modifier
//                        .size(6.dp)
//                        .clip(CircleShape)
//                        .background(color)
//                )

                // Opción B (Comentada): Si quisieras barras tipo evento en el futuro

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(color)
                )

            }
        }
    }
}