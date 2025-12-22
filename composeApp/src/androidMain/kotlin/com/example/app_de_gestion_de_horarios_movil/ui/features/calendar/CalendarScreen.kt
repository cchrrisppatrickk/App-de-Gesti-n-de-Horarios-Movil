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
import com.example.app_de_gestion_de_horarios_movil.ui.features.create_task.CreateEventSheet
import com.example.app_de_gestion_de_horarios_movil.ui.features.create_task.CreateTaskSheet
import com.example.app_de_gestion_de_horarios_movil.ui.features.create_task.CreateTaskViewModel
import kotlinx.datetime.*
import org.koin.androidx.compose.koinViewModel
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(
    // Inyectamos AMBOS ViewModels aquí
    viewModel: CalendarViewModel = koinViewModel(),
    createViewModel: CreateTaskViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    // --- ESTADOS DE CONTROL DE VENTANAS (SHEETS) ---
    var showEventForm by remember { mutableStateOf(false) } // Para Eventos
    var showTaskForm by remember { mutableStateOf(false) }  // Para Tareas

    // ESTADO CLAVE: ¿Qué tarea estamos editando?
    var taskBeingEdited by remember { mutableStateOf<Task?>(null) }

    // Hoja de Resumen del Día
    var showDayListSheet by remember { mutableStateOf(false) }

    // Tarea seleccionada para ver detalle individual
    var selectedTaskForDetail by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CalendarTopBar(
                currentMonth = state.currentMonth,
                onMenuClick = { },
                onSearchClick = { },
                onMoreClick = { }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Crear NUEVO evento (limpiamos edición)
                    taskBeingEdited = null
                    createViewModel.prepareNewTask(state.selectedDate)
                    showEventForm = true
                },
                containerColor = Color(0xFF2196F3),
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
            DaysOfWeekHeader()

            CalendarGrid(
                modifier = Modifier.weight(1f),
                currentMonth = state.currentMonth,
                selectedDate = state.selectedDate,
                tasksMap = state.tasks,
                onDateSelected = { date ->
                    viewModel.onDateSelected(date)
                    showDayListSheet = true
                },
                onPrevMonth = viewModel::onPreviousMonth,
                onNextMonth = viewModel::onNextMonth
            )
        }
    }

    // --- GESTIÓN DE VENTANAS EMERGENTES ---

    // A. FORMULARIO DE EVENTOS (Sin cambios)
    if (showEventForm) {
        CreateEventSheet(
            onDismiss = { showEventForm = false }
        )
    }

    // B. FORMULARIO DE TAREAS (Sin cambios)
    if (showTaskForm) {
        CreateTaskSheet(
            taskToEdit = taskBeingEdited,
            viewModel = createViewModel,
            onDismiss = {
                showTaskForm = false
                taskBeingEdited = null
                // Opcional: Si quieres que al terminar de editar vuelva a la lista del día:
                // showDayListSheet = true
            }
        )
    }

    // C. LISTA DE RESUMEN DEL DÍA
    // CAMBIO: Quitamos la condición `&& selectedTaskForDetail == null`.
    // Ahora controlamos la visibilidad puramente con `showDayListSheet`.
    if (showDayListSheet) {
        CalendarEventDetailSheet(
            date = state.selectedDate,
            taskList = state.tasks[state.selectedDate] ?: emptyList(),
            onDismiss = { showDayListSheet = false },
            onCreateEventClick = {
                createViewModel.prepareNewTask(state.selectedDate)
                showDayListSheet = false // Cerramos lista
                showEventForm = true     // Abrimos formulario
            },
            onItemClick = { task ->
                selectedTaskForDetail = task
                showDayListSheet = false // <--- CLAVE: Cerramos explícitamente la lista
            }
        )
    }

    // D. DETALLE INDIVIDUAL DE TAREA/EVENTO (Actualizado con recurrencia)
    selectedTaskForDetail?.let { task ->
        EventDetailSheet(
            task = task,
            onDismiss = {
                selectedTaskForDetail = null
                showDayListSheet = true // Volvemos a la lista
            },

            // --- ACCIONES DE EDICIÓN ---
            onEdit = {
                // Editar SOLO ESTE (Misma lógica de antes)
                taskBeingEdited = task
                selectedTaskForDetail = null
                showDayListSheet = false

                if (task.type == TaskType.TASK) {
                    showTaskForm = true
                } else {
                    // isGroupEdit = false (Por defecto)
                    createViewModel.setTaskToEdit(task, isGroupEdit = false)
                    showEventForm = true
                }
            },
            onEditAll = {
                // Editar TODA LA SERIE (Nuevo)
                taskBeingEdited = task
                selectedTaskForDetail = null
                showDayListSheet = false

                if (task.type == TaskType.TASK) {
                    // TODO: Asegúrate de que CreateTaskSheet soporte flag de grupo si es necesario
                    // createViewModel.setTaskToEdit(task, isGroupEdit = true)
                    showTaskForm = true
                } else {
                    // Avisamos al VM que es una edición de GRUPO
                    createViewModel.setTaskToEdit(task, isGroupEdit = true)
                    showEventForm = true
                }
            },

            // --- ACCIONES DE ELIMINACIÓN ---
            onDelete = {
                // Borrar SOLO ESTE
                viewModel.onDeleteTask(task)
                selectedTaskForDetail = null
                showDayListSheet = true
            },
            onDeleteAll = {
                // Borrar TODA LA SERIE (Nuevo)
                viewModel.onDeleteAllOccurrences(task)
                selectedTaskForDetail = null
                showDayListSheet = true
            }
        )
    }

}


// --- COMPONENTES AUXILIARES ---

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarTopBar(
    currentMonth: LocalDate,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    val monthName = java.time.Month.of(currentMonth.monthNumber)
        .getDisplayName(TextStyle.SHORT, Locale.getDefault())
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    val title = "$monthName ${currentMonth.year}"

    TopAppBar(
        title = { Text(title, fontSize = 22.sp) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, contentDescription = "Menú") }
        },
        actions = {
            IconButton(onClick = { }) { Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Chat") }
            IconButton(onClick = onMoreClick) { Icon(Icons.Default.MoreVert, contentDescription = "Más") }
        }
    )
}

@Composable
fun DaysOfWeekHeader() {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        listOf("dom", "lun", "mar", "mié", "jue", "vie", "sáb").forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// --- GRID Y CELDAS OPTIMIZADAS (PARA SOLUCIONAR LA LENTITUD) ---

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
    // 1. Cálculos fuera de la composición del item (CRÍTICO PARA RENDIMIENTO)
    val firstDayOfMonth = remember(currentMonth) { LocalDate(currentMonth.year, currentMonth.month, 1) }
    val dayOfWeekIso = firstDayOfMonth.dayOfWeek.isoDayNumber
    val emptySlots = if (dayOfWeekIso == 7) 0 else dayOfWeekIso
    val daysInMonth = remember(currentMonth) {
        currentMonth.month.length(currentMonth.year % 4 == 0 && (currentMonth.year % 100 != 0 || currentMonth.year % 400 == 0))
    }
    val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = modifier.fillMaxWidth().border(BorderStroke(0.5.dp, gridColor)),
        contentPadding = PaddingValues(0.dp)
    ) {
        items(emptySlots) {
            Box(modifier = Modifier.aspectRatio(0.6f).border(BorderStroke(0.25.dp, gridColor)))
        }

        items(daysInMonth) { dayOffset ->
            // Cálculo simple de fecha
            val date = firstDayOfMonth.plus(DatePeriod(days = dayOffset))
            val isToday = date == today
            val isSelected = date == selectedDate

            // Acceso seguro al mapa
            val dayTasks = tasksMap[date] ?: emptyList()
            // Tomamos solo los colores necesarios para pintar (máximo 5)
            val taskColors = dayTasks.take(5).map { it.colorHex }

            DayCell(
                day = date.dayOfMonth,
                isToday = isToday,
                isSelected = isSelected,
                taskColors = taskColors,
                gridColor = gridColor,
                onClick = { onDateSelected(date) }
            )
        }

        // Relleno final para que la cuadrícula se vea completa
        val totalCells = emptySlots + daysInMonth
        val remainingCells = (7 - (totalCells % 7)) % 7
        items(remainingCells) {
            Box(modifier = Modifier.aspectRatio(0.6f).border(BorderStroke(0.25.dp, gridColor)))
        }
    }
}

@Composable
fun DayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    taskColors: List<String>,
    gridColor: Color,
    onClick: () -> Unit
) {
    // EL CLIC DEBE ESTAR EN EL MODIFIER EXTERNO
    Box(
        modifier = Modifier
            .aspectRatio(0.6f)
            .border(BorderStroke(0.25.dp, gridColor))
            .clickable { onClick() } // <--- EL CLIC AQUÍ
            .padding(4.dp)
    ) {
        // Círculo del número
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(28.dp)
                .clip(CircleShape)
                .background(if (isToday) Color(0xFF2196F3) else if (isSelected) Color.LightGray.copy(alpha=0.5f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.toString(),
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }

        // Puntos/Barras de eventos
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            taskColors.forEach { colorHex ->
                val color = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { Color.Gray }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(color)
                )
            }
        }
    }
}