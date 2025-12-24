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

    // Fechas base para cálculos matemáticos rápidos
    val baseDate = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    val firstDayOfBase = remember { LocalDate(baseDate.year, baseDate.month, 1) }

    val initialPage = remember { START_PAGE_INDEX }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { Int.MAX_VALUE })

    // --- TÍTULO DINÁMICO (INSTANTÁNEO) ---
    val displayedDate by remember {
        derivedStateOf {
            // Si estamos en modo MES, usamos el pager. Si no, usamos la fecha seleccionada.
            if (state.viewMode == CalendarViewMode.MONTH) {
                val diff = pagerState.currentPage - initialPage
                firstDayOfBase.plus(DatePeriod(months = diff))
            } else {
                state.selectedDate
            }
        }
    }

    // --- LÓGICA DE DETECCIÓN DE SCROLL Y CARGA ---

    // 1. Detectar cambio de página (Solo en modo MONTH)
    LaunchedEffect(pagerState.currentPage) {
        if (state.viewMode == CalendarViewMode.MONTH) {
            val newDate = displayedDate
            if (newDate.month != state.currentMonth.month || newDate.year != state.currentMonth.year) {
                viewModel.onDateSelected(newDate)
            }
            // Activar temporizador para desvanecer el efecto fantasma
            isGhostVisibleAfterScroll = true
            delay(GHOST_HOLD_DURATION)
            isGhostVisibleAfterScroll = false
        }
    }

    // 2. Sincronización Picker -> Pager
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
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp), // Edge-to-Edge

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
            // --- CONTROLADOR DE VISTAS ---
            when (state.viewMode) {
                CalendarViewMode.MONTH -> {
                    // --- VISTA MENSUAL ---
                    DaysOfWeekHeader()

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) { page ->
                        // Calcular fecha de página
                        val diffFromStart = page - initialPage
                        val pageDate = firstDayOfBase.plus(DatePeriod(months = diffFromStart))
                        val firstDayOfPage = LocalDate(pageDate.year, pageDate.month, 1)

                        Box(modifier = Modifier.fillMaxSize()) {
                            // A. EL GRID
                            CalendarGridFlexible(
                                modifier = Modifier.fillMaxSize(),
                                currentMonth = firstDayOfPage,
                                selectedDate = state.selectedDate,
                                tasksMap = state.tasks,
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
                            showDayListSheet = false // CORRECCIÓN: Lista desactivada
                        },
                        // Este es el callback para el arrastre (Drag-to-Create)
                        /*onRangeSelected = { date, start, end ->
                            taskBeingEdited = null
                            createViewModel.prepareNewTask(date)
                            createViewModel.onStartTimeChange(start)
                            createViewModel.onEndTimeChange(end)
                            showEventForm = true
                        }*/
                        // NOTA: Si en el paso anterior eliminaste 'onRangeSelected' de WeekView para simplificar,
                        // elimina este bloque. Si usaste la versión avanzada, descoméntalo.
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

    // --- DIÁLOGOS Y SHEETS ---
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
        // En vista mensual, al hacer clic en un día mostramos la lista
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
            onDismiss = { selectedTaskForDetail = null;
                // Si estamos en mes, volvemos a la lista del día. Si estamos en Día, cerramos todo.
                if(state.viewMode == CalendarViewMode.MONTH) showDayListSheet = true
            },
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

// --- COMPONENTES UI AUXILIARES ---

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarTopBarClickable(
    currentMonth: LocalDate,
    currentViewMode: CalendarViewMode,
    onDateClick: () -> Unit,
    onViewModeSelected: (CalendarViewMode) -> Unit
) {
    val monthName = java.time.Month.of(currentMonth.monthNumber)
        .getDisplayName(TextStyle.FULL, Locale.getDefault())
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    // LÓGICA DE FORMATO DE TÍTULO DINÁMICO
    val title = remember(currentMonth, currentViewMode) {
        val javaDate = java.time.LocalDate.of(currentMonth.year, currentMonth.monthNumber, currentMonth.dayOfMonth)

        if (currentViewMode == CalendarViewMode.DAY) {
            // Formato para Día: "mié., 24 de dic"
            // EEE = día abreviado, d = número, MMM = mes abreviado
            val formatter = java.time.format.DateTimeFormatter.ofPattern("EEE, d 'de' MMM", Locale.getDefault())
            javaDate.format(formatter).replaceFirstChar { it.titlecase() }
        } else {
            // Formato para Mes: "diciembre 2025"
            val formatter = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
            javaDate.format(formatter).replaceFirstChar { it.titlecase() }
        }
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
            // 1. TÍTULO DE FECHA
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onDateClick() }
                    .padding(horizontal = 8.dp, vertical = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.onBackground)
            }

            // 2. SELECTOR DE VISTA (DROPDOWN)
            Box {
                OutlinedButton(
                    onClick = { showViewMenu = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(text = currentViewMode.title, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(16.dp))
                }

                DropdownMenu(
                    expanded = showViewMenu,
                    onDismissRequest = { showViewMenu = false }
                ) {
                    CalendarViewMode.values().forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.title) },
                            onClick = {
                                onViewModeSelected(mode)
                                showViewMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// --- CALENDAR GRID Y CELDAS (Igual que antes, pero asegurándonos de que están disponibles) ---
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
                            tasks = dayTasks
                        )
                    }
                }
            }
        }
    }
}

// --- UTILS DE COLOR Y CHIPS ---
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
    val backgroundColor = try { Color(android.graphics.Color.parseColor(task.colorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
    val textColor = getContrastColor(task.colorHex)

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
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DayCellContent(day: Int, isToday: Boolean, isSelected: Boolean, isCurrentMonth: Boolean, tasks: List<Task>) {
    Column(
        modifier = Modifier.fillMaxSize().padding(2.dp),
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
        val maxItemsToShow = 3
        val visibleTasks = tasks.take(maxItemsToShow)
        val overflowCount = tasks.size - maxItemsToShow
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            visibleTasks.forEach { task -> TaskChip(task = task) }
            if (overflowCount > 0) {
                Text(text = "+$overflowCount más", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 2.dp))
            }
        }
    }
    if (isSelected && !isToday) {
        Box(modifier = Modifier.fillMaxSize().border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(4.dp)))
    }
}

@Composable
fun DaysOfWeekHeader() {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        listOf("dom", "lun", "mar", "mié", "jue", "vie", "sáb").forEach { day ->
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