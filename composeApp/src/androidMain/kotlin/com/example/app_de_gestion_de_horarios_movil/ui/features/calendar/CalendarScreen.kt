package com.example.app_de_gestion_de_horarios_movil.ui.features.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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

// --- VARIABLES DE AJUSTE ---
private val CalendarHorizontalMargin = 0.dp
private val CellBorderColorAlpha = 0.1f
private val DayNumberFontSize = 12.sp
private val CurrentDayIndicatorSize = 24.dp

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = koinViewModel(),
    createViewModel: CreateTaskViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    // Estados de UI
    var isGroupEditMode by remember { mutableStateOf(false) }
    var showEventForm by remember { mutableStateOf(false) }
    var showTaskForm by remember { mutableStateOf(false) }
    var taskBeingEdited by remember { mutableStateOf<Task?>(null) }
    var showDayListSheet by remember { mutableStateOf(false) }
    var selectedTaskForDetail by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // CLAVE: Esto permite el efecto Edge-to-Edge
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        topBar = {
            CalendarTopBar(currentMonth = state.currentMonth)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    taskBeingEdited = null
                    createViewModel.prepareNewTask(state.selectedDate)
                    showEventForm = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
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
                .padding(horizontal = CalendarHorizontalMargin)
        ) {

            DaysOfWeekHeader()

            HorizontalDivider(
                modifier = Modifier.padding(bottom = 0.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            )

            CalendarGridFlexible(
                modifier = Modifier.weight(1f),
                currentMonth = state.currentMonth,
                selectedDate = state.selectedDate,
                tasksMap = state.tasks,
                onDateSelected = { date ->
                    viewModel.onDateSelected(date)
                    showDayListSheet = true
                }
            )
        }
    }

    // --- MANEJO DE VENTANAS EMERGENTES (SHEETS) ---
    if (showEventForm) CreateEventSheet(onDismiss = { showEventForm = false })

    if (showTaskForm) {
        CreateTaskSheet(
            taskToEdit = taskBeingEdited,
            isGroupEdit = isGroupEditMode,
            viewModel = createViewModel,
            onDismiss = {
                showTaskForm = false
                taskBeingEdited = null
                isGroupEditMode = false
            }
        )
    }

    if (showDayListSheet) {
        CalendarEventDetailSheet(
            date = state.selectedDate,
            taskList = state.tasks[state.selectedDate] ?: emptyList(),
            onDismiss = { showDayListSheet = false },
            onCreateEventClick = {
                createViewModel.prepareNewTask(state.selectedDate)
                showDayListSheet = false
                showEventForm = true
            },
            onItemClick = { task ->
                selectedTaskForDetail = task
                showDayListSheet = false
            }
        )
    }

    selectedTaskForDetail?.let { task ->
        EventDetailSheet(
            task = task,
            onDismiss = { selectedTaskForDetail = null; showDayListSheet = true },
            onEdit = {
                taskBeingEdited = task
                isGroupEditMode = false
                selectedTaskForDetail = null
                showDayListSheet = false
                if (task.type == TaskType.TASK) showTaskForm = true else { createViewModel.setTaskToEdit(task, false); showEventForm = true }
            },
            onEditAll = {
                taskBeingEdited = task
                isGroupEditMode = true
                selectedTaskForDetail = null
                showDayListSheet = false
                if (task.type == TaskType.TASK) showTaskForm = true else { createViewModel.setTaskToEdit(task, true); showEventForm = true }
            },
            onDelete = { viewModel.onDeleteTask(task); selectedTaskForDetail = null; showDayListSheet = true },
            onDeleteAll = { viewModel.onDeleteAllOccurrences(task); selectedTaskForDetail = null; showDayListSheet = true }
        )
    }
}

// --- COMPONENTES AUXILIARES ---

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarTopBar(currentMonth: LocalDate) {
    val monthName = java.time.Month.of(currentMonth.monthNumber)
        .getDisplayName(TextStyle.SHORT, Locale.getDefault())
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    val title = "$monthName ${currentMonth.year}"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding() // Baja el contenido para no tapar la hora/batería
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun DaysOfWeekHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        listOf("dom", "lun", "mar", "mié", "jue", "vie", "sáb").forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarGridFlexible(
    modifier: Modifier = Modifier,
    currentMonth: LocalDate,
    selectedDate: LocalDate,
    tasksMap: Map<LocalDate, List<Task>>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = remember(currentMonth) { LocalDate(currentMonth.year, currentMonth.month, 1) }
    val dayOfWeekIso = firstDayOfMonth.dayOfWeek.isoDayNumber
    // Ajuste para calendario que empieza en Domingo (ISO Lunes=1 ... Domingo=7)
    val startOffset = if (dayOfWeekIso == 7) 0 else dayOfWeekIso

    val daysInMonth = remember(currentMonth) {
        currentMonth.month.length(currentMonth.year % 4 == 0 && (currentMonth.year % 100 != 0 || currentMonth.year % 400 == 0))
    }

    val prevMonth = firstDayOfMonth.minus(DatePeriod(months = 1))
    val daysInPrevMonth = prevMonth.month.length(prevMonth.year % 4 == 0 && (prevMonth.year % 100 != 0 || prevMonth.year % 400 == 0))

    val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    val gridBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = CellBorderColorAlpha)

    Column(modifier = modifier) {
        for (week in 0 until 6) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                for (day in 0 until 7) {
                    val cellIndex = (week * 7) + day
                    val dayValue = cellIndex - startOffset + 1

                    val dateToShow: LocalDate
                    val isCurrentMonth: Boolean

                    if (dayValue <= 0) {
                        val prevDay = daysInPrevMonth + dayValue
                        dateToShow = prevMonth.plus(DatePeriod(days = prevDay - 1))
                        isCurrentMonth = false
                    } else if (dayValue > daysInMonth) {
                        dateToShow = firstDayOfMonth.plus(DatePeriod(days = dayValue - 1))
                        isCurrentMonth = false
                    } else {
                        dateToShow = firstDayOfMonth.plus(DatePeriod(days = dayValue - 1))
                        isCurrentMonth = true
                    }

                    val isToday = dateToShow == today
                    val isSelected = dateToShow == selectedDate
                    val dayTasks = tasksMap[dateToShow] ?: emptyList()
                    val eventColors = dayTasks.take(3).map { it.colorHex }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(width = 0.5.dp, color = gridBorderColor)
                            .clickable { onDateSelected(dateToShow) }
                    ) {
                        DayCellContent(
                            day = dateToShow.dayOfMonth,
                            isToday = isToday,
                            isSelected = isSelected,
                            isCurrentMonth = isCurrentMonth,
                            eventColors = eventColors
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayCellContent(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    isCurrentMonth: Boolean,
    eventColors: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(CurrentDayIndicatorSize)
                .clip(CircleShape)
                .background(if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = DayNumberFontSize),
                color = when {
                    isToday -> Color.White
                    !isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        eventColors.forEach { colorHex ->
            val color = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { Color.Gray }
            Box(
                modifier = Modifier
                    .padding(vertical = 1.dp)
                    .fillMaxWidth(0.9f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }

    if (isSelected && !isToday) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        )
    }
}