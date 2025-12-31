package com.example.app_de_gestion_de_horarios_movil.ui.features.calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.ui.features.calendar.components.MonthYearPickerDialog
import com.example.app_de_gestion_de_horarios_movil.ui.features.calendar.model.CalendarViewMode
import com.example.app_de_gestion_de_horarios_movil.ui.features.calendar.views.DayView
import com.example.app_de_gestion_de_horarios_movil.ui.features.calendar.views.ScheduleView
import com.example.app_de_gestion_de_horarios_movil.ui.features.calendar.views.WeekView
import com.example.app_de_gestion_de_horarios_movil.ui.features.create_task.CreateEventSheet
import com.example.app_de_gestion_de_horarios_movil.ui.features.create_task.CreateTaskSheet
import com.example.app_de_gestion_de_horarios_movil.ui.features.create_task.CreateTaskViewModel
import kotlinx.coroutines.delay
import kotlinx.datetime.*
import org.koin.androidx.compose.koinViewModel
import java.time.format.TextStyle
import java.util.Locale

// --- CONSTANTES DE CONFIGURACIÓN ---
private const val START_PAGE_INDEX = Int.MAX_VALUE / 2
private const val GHOST_HOLD_DURATION = 300L // Tiempo que dura el "fantasma" tras soltar (ms)
private val CellBorderColorAlpha = 0.1f
private val DayNumberFontSize = 12.sp
private val CurrentDayIndicatorSize = 24.dp

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = koinViewModel(),
    createViewModel: CreateTaskViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    // --- ESTADOS LOCALES ---
    var showDatePicker by remember { mutableStateOf(false) }
    var isGroupEditMode by remember { mutableStateOf(false) }
    var showEventForm by remember { mutableStateOf(false) }
    var showTaskForm by remember { mutableStateOf(false) }
    var taskBeingEdited by remember { mutableStateOf<Task?>(null) }
    var showDayListSheet by remember { mutableStateOf(false) }
    var selectedTaskForDetail by remember { mutableStateOf<Task?>(null) }

    // Estado para mantener el overlay visible después del scroll
    var isGhostVisibleAfterScroll by remember { mutableStateOf(false) }

    // Fechas base
    val baseDate = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    val firstDayOfBase = remember { LocalDate(baseDate.year, baseDate.month, 1) }

    val initialPage = remember { START_PAGE_INDEX }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { Int.MAX_VALUE })

    // --- TÍTULO DINÁMICO ---
    val displayedDate by remember {
        derivedStateOf {
            if (state.viewMode == CalendarViewMode.MONTH) {
                val diff = pagerState.currentPage - initialPage
                firstDayOfBase.plus(DatePeriod(months = diff))
            } else {
                state.selectedDate
            }
        }
    }

    // --- DETECCIÓN DE SCROLL ---
    LaunchedEffect(pagerState.currentPage) {
        if (state.viewMode == CalendarViewMode.MONTH) {
            val newDate = displayedDate
            if (newDate.month != state.currentMonth.month || newDate.year != state.currentMonth.year) {
                viewModel.onDateSelected(newDate)
            }
            isGhostVisibleAfterScroll = true
            delay(GHOST_HOLD_DURATION)
            isGhostVisibleAfterScroll = false
        }
    }

    // Sincronización Picker -> Pager
    LaunchedEffect(state.currentMonth) {
        if (state.viewMode == CalendarViewMode.MONTH &&
            (state.currentMonth.month != displayedDate.month || state.currentMonth.year != displayedDate.year)) {
            val monthsDiff = (state.currentMonth.year - firstDayOfBase.year) * 12 +
                    (state.currentMonth.monthNumber - firstDayOfBase.monthNumber)
            val targetPage = initialPage + monthsDiff
            if (pagerState.currentPage != targetPage) {
                pagerState.scrollToPage(targetPage)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            CalendarTopBarClickable(
                currentMonth = displayedDate,
                currentViewMode = state.viewMode,
                onDateClick = { showDatePicker = true },
                onViewModeSelected = { viewModel.onViewModeChanged(it) }
            )
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
                Icon(Icons.Default.Add, contentDescription = "Crear")
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state.viewMode) {
                CalendarViewMode.MONTH -> {
                    // --- VISTA MENSUAL OPTIMIZADA ---
                    DaysOfWeekHeader()

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) { page ->
                        // Calcular mes de la página
                        val diffFromStart = page - initialPage
                        val pageDate = remember(diffFromStart) { firstDayOfBase.plus(DatePeriod(months = diffFromStart)) }
                        val firstDayOfPage = remember(pageDate) { LocalDate(pageDate.year, pageDate.month, 1) }

                        Box(modifier = Modifier.fillMaxSize()) {
                            // A. EL GRID (Optimizado)
                            CalendarGridFlexible(
                                modifier = Modifier.fillMaxSize(),
                                currentMonth = firstDayOfPage,
                                selectedDate = state.selectedDate,
                                tasksMap = state.tasks, // Pasamos el mapa, el filtrado ocurre dentro con remember
                                onDateSelected = { date ->
                                    viewModel.onDateSelected(date)
                                    showDayListSheet = true
                                }
                            )

                            // B. EFECTO FANTASMA
                            val showGhost = pagerState.isScrollInProgress || isGhostVisibleAfterScroll || state.isLoading
                            this@Column.AnimatedVisibility(
                                visible = showGhost,
                                enter = fadeIn(animationSpec = tween(100)),
                                exit = fadeOut(animationSpec = tween(500)),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = java.time.Month.of(pageDate.monthNumber)
                                            .getDisplayName(TextStyle.FULL, Locale.getDefault())
                                            .uppercase(),
                                        style = MaterialTheme.typography.displayLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 4.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.rotate(-15f).fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                CalendarViewMode.DAY -> {
                    DayView(
                        selectedDate = state.selectedDate,
                        tasksMap = state.tasks,
                        onDateChange = { newDate ->
                            viewModel.onDateSelected(newDate)
                        },
                        onTaskClick = { task ->
                            selectedTaskForDetail = task
                            showDayListSheet = false
                        },
                        // --- NUEVO CALLBACK ---
                        onEmptySlotClick = { dateTime ->
                            // 1. Limpiamos cualquier edición previa
                            taskBeingEdited = null

                            // 2. Preparamos el ViewModel con la fecha Y LA HORA clickeada
                            // Asegúrate de tener una función que acepte LocalTime o configurar los campos manualmente
                            createViewModel.prepareNewTask(dateTime.date)
                            createViewModel.onStartTimeChange(dateTime.time)
                            // Opcional: Configurar fin 1 hora después por defecto
                            createViewModel.onEndTimeChange(dateTime.time.let {
                                // Lógica simple para sumar 1 hora
                                val nextHour = if (it.hour == 23) 23 else it.hour + 1
                                val min = it.minute
                                kotlinx.datetime.LocalTime(nextHour, min)
                            })

                            // 3. Abrimos el formulario
                            showEventForm = true
                            // O showTaskForm = true, dependiendo de qué quieras crear por defecto
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                CalendarViewMode.WEEK -> {
                    WeekView(
                        selectedDate = state.selectedDate,
                        tasks = state.tasks,
                        onDateSelected = { newDate -> viewModel.onDateSelected(newDate) },
                        onTaskClick = { task ->
                            selectedTaskForDetail = task
                            showDayListSheet = false
                        },
                        // --- ESTA ES LA PARTE QUE FALTABA ---
                        onRangeSelected = { date, start, end ->
                            taskBeingEdited = null
                            // Preparamos el ViewModel con los datos precisos del arrastre
                            createViewModel.prepareNewTask(date)
                            createViewModel.onStartTimeChange(start)
                            createViewModel.onEndTimeChange(end)
                            // Abrimos el formulario
                            showEventForm = true
                        }
                    )
                }

                CalendarViewMode.SCHEDULE -> {
                    ScheduleView(
                        tasksMap = state.tasks,
                        onTaskClick = { task ->
                            selectedTaskForDetail = task
                            showDayListSheet = false // CORRECCIÓN: Lista desactivada
                        }
                    )
                }
            }
        }
    }

    // --- DIÁLOGOS Y SHEETS (Sin cambios mayores) ---
    if (showDatePicker) {
        MonthYearPickerDialog(
            visible = true,
            currentMonth = displayedDate.monthNumber,
            currentYear = displayedDate.year,
            onDismiss = { showDatePicker = false },
            onConfirm = { month, year ->
                val newDate = LocalDate(year, month, 1)
                viewModel.onDateSelected(newDate)
                showDatePicker = false
            }
        )
    }

    if (showEventForm) {
        CreateEventSheet(onDismiss = { showEventForm = false })
    }

    if (showTaskForm) {
        CreateTaskSheet(
            taskToEdit = taskBeingEdited,
            isGroupEdit = isGroupEditMode,
            viewModel = createViewModel,
            onDismiss = { showTaskForm = false; taskBeingEdited = null; isGroupEditMode = false }
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
            onItemClick = { task -> selectedTaskForDetail = task; showDayListSheet = false }
        )
    }

    selectedTaskForDetail?.let { task ->
        EventDetailSheet(
            task = task,
            onDismiss = { selectedTaskForDetail = null; if(state.viewMode == CalendarViewMode.MONTH) showDayListSheet = true },
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
            onDelete = { viewModel.onDeleteTask(task); selectedTaskForDetail = null; if(state.viewMode == CalendarViewMode.MONTH) showDayListSheet = true },
            onDeleteAll = { viewModel.onDeleteAllOccurrences(task); selectedTaskForDetail = null; if(state.viewMode == CalendarViewMode.MONTH) showDayListSheet = true }
        )
    }
}

// --- HELPER PARA ROTACIÓN ---
fun Modifier.rotate(degrees: Float) = this.then(Modifier.graphicsLayer(rotationZ = degrees))

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarTopBarClickable(
    currentMonth: LocalDate,
    currentViewMode: CalendarViewMode,
    onDateClick: () -> Unit,
    onViewModeSelected: (CalendarViewMode) -> Unit
) {
    // Cálculo optimizado del título
    val title = remember(currentMonth, currentViewMode) {
        val javaDate = java.time.LocalDate.of(currentMonth.year, currentMonth.monthNumber, currentMonth.dayOfMonth)
        val formatter = if (currentViewMode == CalendarViewMode.DAY) {
            java.time.format.DateTimeFormatter.ofPattern("EEE, d 'de' MMM", Locale.getDefault())
        } else {
            java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        }
        javaDate.format(formatter).replaceFirstChar { it.titlecase() }
    }
    var showViewMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(top = 12.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onDateClick() }
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.onBackground)
            }

            Box {
                OutlinedButton(
                    onClick = { showViewMenu = true },
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(text = currentViewMode.title, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(16.dp))
                }
                DropdownMenu(expanded = showViewMenu, onDismissRequest = { showViewMenu = false }) {
                    CalendarViewMode.values().forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.title) },
                            onClick = { onViewModeSelected(mode); showViewMenu = false }
                        )
                    }
                }
            }
        }
    }
}

// --- CALENDAR GRID OPTIMIZADO ---
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarGridFlexible(
    modifier: Modifier = Modifier,
    currentMonth: LocalDate,
    selectedDate: LocalDate,
    tasksMap: Map<LocalDate, List<Task>>,
    onDateSelected: (LocalDate) -> Unit
) {
    // 1. OPTIMIZACIÓN: Pre-calcular la lista de días (State Derivation)
    // Esto evita recalcular las fechas y filtrar el mapa 126 veces por frame.
    // Solo se recalcula si cambiamos de mes o si cambian las tareas.
    val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }

    val daysList = remember(currentMonth, selectedDate, tasksMap) {
        val firstDayOfMonth = LocalDate(currentMonth.year, currentMonth.month, 1)
        val dayOfWeekIso = firstDayOfMonth.dayOfWeek.isoDayNumber
        val startOffset = if (dayOfWeekIso == 7) 0 else dayOfWeekIso

        val daysInMonth = currentMonth.month.length(currentMonth.year % 4 == 0 && (currentMonth.year % 100 != 0 || currentMonth.year % 400 == 0))
        val prevMonth = firstDayOfMonth.minus(DatePeriod(months = 1))
        val daysInPrevMonth = prevMonth.month.length(prevMonth.year % 4 == 0 && (prevMonth.year % 100 != 0 || prevMonth.year % 400 == 0))

        val list = mutableListOf<CalendarDayUiModel>()

        for (i in 0 until 42) { // 6 semanas * 7 días
            val dayValue = i - startOffset + 1
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

            list.add(
                CalendarDayUiModel(
                    date = dateToShow,
                    dayValue = dateToShow.dayOfMonth,
                    isCurrentMonth = isCurrentMonth,
                    isToday = dateToShow == today,
                    isSelected = dateToShow == selectedDate,
                    tasks = tasksMap[dateToShow] ?: emptyList()
                )
            )
        }
        list
    }

    val gridBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = CellBorderColorAlpha)

    Column(modifier = modifier) {
        for (week in 0 until 6) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                for (day in 0 until 7) {
                    val cellIndex = (week * 7) + day
                    val dayModel = daysList[cellIndex]

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(width = 0.5.dp, color = gridBorderColor)
                            .clickable { onDateSelected(dayModel.date) }
                    ) {
                        DayCellContent(dayModel = dayModel)
                    }
                }
            }
        }
    }
}

// --- UTILS DE COLOR OPTIMIZADOS ---
// Memorizar el contraste para evitar cálculos repetidos
fun getContrastColor(backgroundHex: String): Color {
    return try {
        val color = Color(android.graphics.Color.parseColor(backgroundHex))
        val luminance = (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
        if (luminance > 0.5) Color.Black else Color.White
    } catch (e: Exception) {
        Color.White
    }
}

@Composable
fun TaskChip(task: Task, modifier: Modifier = Modifier) {
    // 2. OPTIMIZACIÓN: Recordar el color parseado
    val backgroundColor = remember(task.colorHex) {
        try { Color(android.graphics.Color.parseColor(task.colorHex)) } catch (e: Exception) { Color.Gray }
    }
    val textColor = remember(task.colorHex) { getContrastColor(task.colorHex) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = task.title,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DayCellContent(dayModel: CalendarDayUiModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Indicador de día (Círculo)
        Box(
            modifier = Modifier
                .size(CurrentDayIndicatorSize)
                .clip(CircleShape)
                .background(if (dayModel.isToday) MaterialTheme.colorScheme.primary else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayModel.dayValue.toString(),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = DayNumberFontSize),
                color = when {
                    dayModel.isToday -> Color.White
                    !dayModel.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (dayModel.isToday) FontWeight.Bold else FontWeight.Normal
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Chips de tareas
        val maxItemsToShow = 3
        val taskCount = dayModel.tasks.size

        // Renderizamos solo los visibles
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            for (i in 0 until minOf(taskCount, maxItemsToShow)) {
                TaskChip(task = dayModel.tasks[i])
            }
            if (taskCount > maxItemsToShow) {
                Text(
                    text = "+${taskCount - maxItemsToShow} más",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }
    }

    // Borde de selección
    if (dayModel.isSelected && !dayModel.isToday) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
        )
    }
}

@Composable
fun DaysOfWeekHeader() {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        val days = remember { listOf("dom", "lun", "mar", "mié", "jue", "vie", "sáb") }
        days.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}
// Modelo ligero para evitar recálculos en la UI
data class CalendarDayUiModel(
    val date: LocalDate,
    val dayValue: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    val tasks: List<Task>
)