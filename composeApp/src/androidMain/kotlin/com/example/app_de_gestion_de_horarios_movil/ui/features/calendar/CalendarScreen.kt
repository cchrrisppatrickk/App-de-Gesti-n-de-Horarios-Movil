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
import com.example.app_de_gestion_de_horarios_movil.ui.features.calendar.components.MonthYearPickerDialog
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
private const val GHOST_HOLD_DURATION = 500L // Tiempo que dura el "fantasma" tras soltar (ms)
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
            val diff = pagerState.currentPage - initialPage
            firstDayOfBase.plus(DatePeriod(months = diff))
        }
    }

    // --- LÓGICA DE DETECCIÓN DE SCROLL Y CARGA ---

    // 1. Detectar cambio de página (Snap finalizado)
    LaunchedEffect(pagerState.currentPage) {
        // Cargar datos reales en el ViewModel
        val newDate = displayedDate
        if (newDate.month != state.currentMonth.month || newDate.year != state.currentMonth.year) {
            viewModel.onDateSelected(newDate)
        }

        // Activar temporizador para desvanecer el efecto fantasma
        isGhostVisibleAfterScroll = true
        delay(GHOST_HOLD_DURATION)
        isGhostVisibleAfterScroll = false
    }

    // 2. Sincronización Picker -> Pager (Si usas el diálogo de fecha)
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
        // CLAVE: Eliminar insets para Edge-to-Edge real
        // Edge-to-Edge: Sin insets para que el contenido suba
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),

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

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                // Calcular qué mes toca dibujar en ESTA página específica del Pager
                val diffFromStart = page - initialPage
                val pageDate = firstDayOfBase.plus(DatePeriod(months = diffFromStart))
                val firstDayOfPage = LocalDate(pageDate.year, pageDate.month, 1)

                Box(modifier = Modifier.fillMaxSize()) {

                    // A. EL CALENDARIO (Capa Inferior)
                    CalendarGridFlexible(
                        modifier = Modifier.fillMaxSize(),
                        currentMonth = firstDayOfPage,
                        selectedDate = state.selectedDate,
                        tasksMap = state.tasks, // El mapa viene del ViewModel
                        onDateSelected = { date ->
                            viewModel.onDateSelected(date)
                            showDayListSheet = true
                        }
                    )

                    // B. LÓGICA DE VISIBILIDAD DEL EFECTO FANTASMA (Capa Superior)
                    val showGhost = pagerState.isScrollInProgress || isGhostVisibleAfterScroll || state.isLoading

                    this@Column.AnimatedVisibility(
                        visible = showGhost,
                        // Aparece rápido, desaparece lento
                        enter = fadeIn(animationSpec = tween(100)),
                        exit = fadeOut(animationSpec = tween(500)),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // C. EL OVERLAY
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                // Fondo semi-transparente que difumina el grid de abajo
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
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), // Marca de agua sutil
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

// --- HELPER PARA ROTACIÓN ---
fun Modifier.rotate(degrees: Float) = this.then(Modifier.graphicsLayer(rotationZ = degrees))

// --- COMPONENTES UI AUXILIARES ---

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarTopBarClickable(currentMonth: LocalDate, onClick: () -> Unit) {
    val monthName = java.time.Month.of(currentMonth.monthNumber)
        .getDisplayName(TextStyle.SHORT, Locale.getDefault())
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    val title = "$monthName ${currentMonth.year}"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding() // Padding para evitar la barra de estado
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
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.KeyboardArrowDown, "Cambiar mes", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun DaysOfWeekHeader() {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
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
    // 1. Variables de configuración del mes
    val firstDayOfMonth = remember(currentMonth) { LocalDate(currentMonth.year, currentMonth.month, 1) }
    val dayOfWeekIso = firstDayOfMonth.dayOfWeek.isoDayNumber
    val startOffset = if (dayOfWeekIso == 7) 0 else dayOfWeekIso

    val daysInMonth = remember(currentMonth) {
        currentMonth.month.length(currentMonth.year % 4 == 0 && (currentMonth.year % 100 != 0 || currentMonth.year % 400 == 0))
    }

    // Calcular mes anterior para rellenar huecos
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

                    // --- CÁLCULO DE LA FECHA (Esto define dateToShow) ---
                    // Esta es la parte que probablemente te faltaba:
                    val dateToShow: LocalDate
                    val isCurrentMonth: Boolean

                    if (dayValue <= 0) {
                        // Es del mes anterior
                        val prevDay = daysInPrevMonth + dayValue
                        dateToShow = prevMonth.plus(DatePeriod(days = prevDay - 1))
                        isCurrentMonth = false
                    } else if (dayValue > daysInMonth) {
                        // Es del mes siguiente
                        dateToShow = firstDayOfMonth.plus(DatePeriod(days = dayValue - 1))
                        isCurrentMonth = false
                    } else {
                        // Es del mes actual
                        dateToShow = firstDayOfMonth.plus(DatePeriod(days = dayValue - 1))
                        isCurrentMonth = true
                    }
                    // ----------------------------------------------------

                    val isToday = dateToShow == today
                    val isSelected = dateToShow == selectedDate

                    // Obtenemos la LISTA DE TAREAS completa para pasarla al nuevo componente
                    val dayTasks = tasksMap[dateToShow] ?: emptyList()

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(width = 0.5.dp, color = gridBorderColor)
                            .clickable { onDateSelected(dateToShow) }
                    ) {
                        // Llamada al componente visual actualizado
                        DayCellContent(
                            day = dateToShow.dayOfMonth,
                            isToday = isToday,
                            isSelected = isSelected,
                            isCurrentMonth = isCurrentMonth,
                            tasks = dayTasks // Pasamos la lista de objetos Task
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
    tasks: List<Task> // CAMBIO: Recibimos la lista completa de Tareas, no solo colores
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp), // Padding general de la celda
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. NÚMERO DEL DÍA
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

        // 2. LISTA DE TAREAS (Estilo Google Calendar)
        // Definimos cuántos caben antes de desbordar
        val maxItemsToShow = 4
        val visibleTasks = tasks.take(maxItemsToShow)
        val overflowCount = tasks.size - maxItemsToShow

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            visibleTasks.forEach { task ->
                TaskChip(task = task)
            }

            // 3. INDICADOR DE DESBORDAMIENTO (+X)
            if (overflowCount > 0) {
                Text(
                    text = "+$overflowCount más",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }
    }

    // Indicador sutil de selección (Borde o fondo ligero)
    if (isSelected && !isToday) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
        )
    }
}



// --- AGREGAR ESTAS FUNCIONES AL FINAL DEL ARCHIVO O EN UN UTILS ---

/**
 * Calcula si el color de fondo es oscuro o claro para poner texto blanco o negro.
 */
fun getContrastColor(backgroundHex: String): Color {
    return try {
        val color = Color(android.graphics.Color.parseColor(backgroundHex))
        // Fórmula de luminancia estándar
        val luminance = (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
        if (luminance > 0.5) Color.Black else Color.White
    } catch (e: Exception) {
        Color.White // Por defecto si falla el parseo
    }
}

/**
 * Componente visual para un item (Chip) de tarea/evento
 */
@Composable
fun TaskChip(
    task: Task,
    modifier: Modifier = Modifier
) {
    val backgroundColor = try {
        Color(android.graphics.Color.parseColor(task.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val textColor = getContrastColor(task.colorHex)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp) // Pequeño espacio entre items
            .clip(RoundedCornerShape(4.dp)) // Bordes redondeados como en la imagen
            .background(backgroundColor)
            .padding(horizontal = 4.dp, vertical = 2.dp) // Padding interno del texto
    ) {
        Text(
            text = task.title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp, // Letra pequeña para que quepa
                fontWeight = FontWeight.Medium
            ),
            color = textColor,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}


