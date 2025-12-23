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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
private const val TRANSITION_DURATION = 500L // Duración del efecto de "pantalla de carga" (ms)
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

    // Estado para forzar la animación visual de transición
    var isVisualLoading by remember { mutableStateOf(false) }

    // Fecha base inmutable para cálculos rápidos de Pager
    val baseDate = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    val firstDayOfBase = remember { LocalDate(baseDate.year, baseDate.month, 1) }

    // --- CONFIGURACIÓN DEL PAGER ---
    val initialPage = remember { START_PAGE_INDEX }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { Int.MAX_VALUE })

    // Título calculado matemáticamente (Actualización instantánea al deslizar)
    val displayedDate by remember {
        derivedStateOf {
            val diff = pagerState.currentPage - initialPage
            firstDayOfBase.plus(DatePeriod(months = diff))
        }
    }

    // --- LÓGICA DE TRANSICIÓN Y CARGA ---
    // Se dispara cada vez que cambia la página (Swipe)
    LaunchedEffect(pagerState.currentPage) {
        // 1. Activar efecto visual
        isVisualLoading = true

        // 2. Solicitar datos reales al ViewModel
        val newDate = displayedDate
        if (newDate.month != state.currentMonth.month || newDate.year != state.currentMonth.year) {
            viewModel.onDateSelected(newDate)
        }

        // 3. Esperar tiempo forzado para el efecto "cinematográfico"
        delay(TRANSITION_DURATION)

        // 4. Desactivar efecto
        isVisualLoading = false
    }

    // Sincronización Inversa: Picker -> Pager
    // Si se cambia el mes desde el diálogo, movemos el pager a la posición correcta
    LaunchedEffect(state.currentMonth) {
        if (state.currentMonth.month != displayedDate.month || state.currentMonth.year != displayedDate.year) {
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
        contentWindowInsets = WindowInsets(0.dp), // Edge-to-Edge Real
        topBar = {
            CalendarTopBarClickable(
                currentMonth = displayedDate,
                onClick = { showDatePicker = true }
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
            DaysOfWeekHeader()

            // Scroll Horizontal Infinito
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val diffFromStart = page - initialPage
                val pageDate = firstDayOfBase.plus(DatePeriod(months = diffFromStart))
                val firstDayOfPage = LocalDate(pageDate.year, pageDate.month, 1)

                Box(modifier = Modifier.fillMaxSize()) {

                    // A. EL CALENDARIO (Fondo)
                    CalendarGridFlexible(
                        modifier = Modifier.fillMaxSize(),
                        currentMonth = firstDayOfPage,
                        selectedDate = state.selectedDate,
                        tasksMap = state.tasks, // Puede estar vacío momentáneamente
                        onDateSelected = { date ->
                            viewModel.onDateSelected(date)
                            showDayListSheet = true
                        }
                    )

                    // B. LA CAPA DE TRANSICIÓN (Overlay)
                    // Se activa si estamos cargando datos O si estamos en tiempo de transición forzada
                    val showOverlay = (state.isLoading || isVisualLoading) && (page == pagerState.currentPage)

                    this@Column.AnimatedVisibility(
                        visible = showOverlay,
                        enter = fadeIn(animationSpec = tween(200)),
                        exit = fadeOut(animationSpec = tween(400)),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                // Fondo casi sólido para ocultar el "salto" de días
                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Nombre del Mes Gigante
                            Text(
                                text = java.time.Month.of(pageDate.monthNumber)
                                    .getDisplayName(TextStyle.FULL, Locale.getDefault())
                                    .uppercase(),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 8.sp // Espaciado elegante
                                ),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .rotate(-15f)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    // --- GESTIÓN DE SHEETS Y DIÁLOGOS ---

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

    if (showEventForm) CreateEventSheet(onDismiss = { showEventForm = false })

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
fun CalendarTopBarClickable(
    currentMonth: LocalDate,
    onClick: () -> Unit
) {
    val monthName = java.time.Month.of(currentMonth.monthNumber)
        .getDisplayName(TextStyle.SHORT, Locale.getDefault())
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    val title = "$monthName ${currentMonth.year}"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding() // Respeta la zona de notificaciones
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onClick() }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Cambiar mes",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp)
            )
        }
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
    // Ajuste ISO: Si domingo=7 y quieres que empiece en domingo, usa offset.
    // Aquí asumimos Lunes=1..Domingo=7, y grid empieza en Domingo.
    val startOffset = if (dayOfWeekIso == 7) 0 else dayOfWeekIso

    val daysInMonth = remember(currentMonth) {
        currentMonth.month.length(currentMonth.year % 4 == 0 && (currentMonth.year % 100 != 0 || currentMonth.year % 400 == 0))
    }

    val prevMonth = firstDayOfMonth.minus(DatePeriod(months = 1))
    val daysInPrevMonth = prevMonth.month.length(prevMonth.year % 4 == 0 && (prevMonth.year % 100 != 0 || prevMonth.year % 400 == 0))

    val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    val gridBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = CellBorderColorAlpha)

    Column(modifier = modifier) {
        // 6 Filas fijas para estabilidad visual
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
        // Indicador numérico
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

        // Barras de eventos
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthYearPickerDialog(
    visible: Boolean,
    currentMonth: Int,
    currentYear: Int,
    onDismiss: () -> Unit,
    onConfirm: (month: Int, year: Int) -> Unit
) {
    if (!visible) return

    var tempMonth by remember { mutableIntStateOf(currentMonth) }
    var tempYear by remember { mutableIntStateOf(currentYear) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Seleccionar fecha",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Lista de Meses
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .height(150.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f), RoundedCornerShape(8.dp)),
                        state = rememberLazyListState(initialFirstVisibleItemIndex = (tempMonth - 1).coerceAtLeast(0))
                    ) {
                        items(java.time.Month.values()) { month ->
                            val isSelected = month.value == tempMonth
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .clickable { tempMonth = month.value },
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = month.getDisplayName(TextStyle.FULL, Locale.getDefault()).replaceFirstChar { it.titlecase() },
                                    modifier = Modifier.padding(start = 16.dp),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Selector de Año
                    Column(
                        modifier = Modifier.weight(0.6f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(onClick = { tempYear++ }) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Next Year")
                        }
                        Text(
                            text = tempYear.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { tempYear-- }) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Prev Year")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onConfirm(tempMonth, tempYear) }) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}

// Extension helper para rotación
fun Modifier.rotate(degrees: Float) = this.then(Modifier.graphicsLayer(rotationZ = degrees))