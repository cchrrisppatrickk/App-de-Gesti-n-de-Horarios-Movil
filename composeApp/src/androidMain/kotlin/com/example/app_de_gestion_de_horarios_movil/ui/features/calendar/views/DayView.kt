package com.example.app_de_gestion_de_horarios_movil.ui.features.calendar.views

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.ui.features.calendar.getContrastColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.datetime.*

// Configuración de dimensiones
private val HOUR_HEIGHT = 60.dp
private val TIME_COLUMN_WIDTH = 50.dp
private const val START_PAGE_INDEX = Int.MAX_VALUE / 2

// --- ESTRUCTURAS PARA EL LAYOUT DE TAREAS SOLAPADAS ---

private data class DayViewTaskUi(
    val task: Task,
    val top: Dp,
    val height: Dp,
    val leftOffsetPercent: Float,
    val widthPercent: Float
)

private fun processTasksForDayView(tasks: List<Task>): List<DayViewTaskUi> {
    if (tasks.isEmpty()) return emptyList()

    val sortedTasks = tasks.filter { !it.isAllDay }.sortedWith(
        compareBy<Task> { it.startTime }.thenByDescending { it.durationMinutes }
    )

    if (sortedTasks.isEmpty()) return emptyList()

    val columns = mutableListOf<MutableList<Task>>()

    for (task in sortedTasks) {
        var placed = false
        for (col in columns) {
            val lastTaskInCol = col.last()
            if (task.startTime >= lastTaskInCol.endTime) {
                col.add(task)
                placed = true
                break
            }
        }
        if (!placed) {
            columns.add(mutableListOf(task))
        }
    }

    val processedTasks = mutableListOf<DayViewTaskUi>()
    val totalColumns = columns.size
    val colWidthPercent = 1f / totalColumns

    columns.forEachIndexed { colIndex, tasksInCol ->
        tasksInCol.forEach { task ->
            val (top, height) = calculateTaskPosition(task.startTime, task.endTime)
            processedTasks.add(
                DayViewTaskUi(
                    task = task,
                    top = top,
                    height = height,
                    leftOffsetPercent = colIndex * colWidthPercent,
                    widthPercent = colWidthPercent
                )
            )
        }
    }

    return processedTasks
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DayView(
    selectedDate: LocalDate,
    tasksMap: Map<LocalDate, List<Task>>,
    onDateChange: (LocalDate) -> Unit,
    onTaskClick: (Task) -> Unit,
    onEmptySlotClick: (LocalDateTime) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    val initialPage = remember { START_PAGE_INDEX }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { Int.MAX_VALUE })

    // Usamos rememberUpdatedState para evitar problemas de cierre en lambdas si selectedDate cambia
    val currentSelectedDate by rememberUpdatedState(selectedDate)

    // Estado local para la fecha base del Pager
    // Truco: Usamos la fecha seleccionada inicial como ancla
    val anchorDate = remember { selectedDate }

    var tempNewEventTime by remember { mutableStateOf<LocalDateTime?>(null) }

    // Limpiar selección temporal si cambiamos de fecha explícitamente
    LaunchedEffect(selectedDate) {
        tempNewEventTime = null
    }

    // Sincronización Pager -> Fecha (Swipe)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.distinctUntilChanged().collect { page ->
            val diffDays = page - initialPage
            val newDate = anchorDate.plus(DatePeriod(days = diffDays))
            if (newDate != currentSelectedDate) {
                onDateChange(newDate)
            }
        }
    }

    // Sincronización Fecha -> Pager (Click en calendario externo)
    LaunchedEffect(selectedDate) {
        val daysDiff = selectedDate.toEpochDays() - anchorDate.toEpochDays()
        val targetPage = initialPage + daysDiff
        if (pagerState.currentPage != targetPage) {
            pagerState.scrollToPage(targetPage)
        }
    }

    // Reloj actual (Actualiza cada minuto)
    var currentTime by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            delay(60_000L)
        }
    }

    HorizontalPager(state = pagerState, modifier = modifier) { page ->
        val diffDays = page - initialPage
        val pageDate = anchorDate.plus(DatePeriod(days = diffDays))
        val tasksForDay = tasksMap[pageDate] ?: emptyList()
        val isToday = pageDate == currentTime.date

        // Procesamiento de tareas (Memoizado)
        val dayViewTasks = remember(tasksForDay) { processTasksForDayView(tasksForDay) }
        val isTempEventOnThisPage = tempNewEventTime?.date == pageDate

        Column(modifier = Modifier.verticalScroll(scrollState)) {

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HOUR_HEIGHT * 24)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val hourHeightPx = with(density) { HOUR_HEIGHT.toPx() }

                            // 1. Calcular en qué índice de hora cayó el click (0 a 23)
                            // Al hacer toInt() eliminamos los decimales, logrando el efecto de "Cuadro entero"
                            val clickedHourIndex = (offset.y / hourHeightPx).toInt().coerceIn(0, 23)

                            // 2. Definir la hora exacta (Minutos siempre en 00)
                            // Esto asegura que si tocas a las 5:45, el sistema tome las 5:00
                            val clickedTime = LocalTime(hour = clickedHourIndex, minute = 0)

                            val clickedDateTime = pageDate.atTime(clickedTime)
                            tempNewEventTime = clickedDateTime
                        }
                    }
            ) {
                val availableWidth = maxWidth - TIME_COLUMN_WIDTH - 4.dp

                // 1. LÍNEAS DE FONDO Y ETIQUETAS DE HORA
                for (hour in 0..23) {
                    val topOffset = HOUR_HEIGHT * hour

                    // Texto de la hora (Ej: 05:00)
                    Text(
                        text = String.format("%02d:00", hour),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .width(TIME_COLUMN_WIDTH)
                            .padding(top = topOffset + 4.dp, end = 8.dp),
                        textAlign = TextAlign.End
                    )

                    // Línea divisoria
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(start = TIME_COLUMN_WIDTH, top = topOffset)
                            .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }

                // 2. TAREAS EXISTENTES
                dayViewTasks.forEach { uiTask ->
                    val taskWidth = availableWidth * uiTask.widthPercent
                    val taskLeftOffset = TIME_COLUMN_WIDTH + 2.dp + (availableWidth * uiTask.leftOffsetPercent)

                    TaskBlock(
                        task = uiTask.task,
                        modifier = Modifier
                            .absoluteOffset(x = taskLeftOffset, y = uiTask.top)
                            .width(taskWidth)
                            .height(uiTask.height)
                            .clickable { onTaskClick(uiTask.task) }
                    )
                }

                // 3. INDICADOR DE HORA ACTUAL (Línea Roja)
                if (isToday) {
                    CurrentTimeIndicator(time = currentTime.time, modifier = Modifier.fillMaxWidth())
                }

                // 4. NUEVO EVENTO TEMPORAL (GHOST BOX)
                // Se dibuja si el usuario ha tocado un hueco en este día
                if (isTempEventOnThisPage && tempNewEventTime != null) {
                    val start = tempNewEventTime!!

                    // Calculamos el Fin: Inicio + 1 Hora exacta
                    val timeZone = TimeZone.currentSystemDefault()
                    val startInstant = start.toInstant(timeZone)
                    val endInstant = startInstant.plus(1, DateTimeUnit.HOUR, timeZone)
                    val end = endInstant.toLocalDateTime(timeZone)

                    // Obtenemos coordenadas para dibujar el cuadro exacto de 1 hora
                    val (topDp, heightDp) = calculateTaskPosition(start, end)

                    NewEventPlaceholderBlock(
                        modifier = Modifier
                            .padding(start = TIME_COLUMN_WIDTH + 2.dp, end = 2.dp)
                            .fillMaxWidth()
                            .offset(y = topDp)
                            .height(heightDp) // Esto será exactamente HOUR_HEIGHT (60.dp)
                            .clickable {
                                onEmptySlotClick(start)
                                tempNewEventTime = null
                            }
                    )
                }
            }
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun CurrentTimeIndicator(
    time: LocalTime,
    modifier: Modifier = Modifier
) {
    val minutes = time.hour * 60 + time.minute
    val topOffset = HOUR_HEIGHT * (minutes / 60f)

    Box(
        modifier = modifier.offset(y = topOffset).height(2.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.padding(start = TIME_COLUMN_WIDTH).fillMaxWidth().align(Alignment.Center),
            color = Color.Red,
            thickness = 1.dp
        )
        Box(
            modifier = Modifier.width(TIME_COLUMN_WIDTH).align(Alignment.CenterStart),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(modifier = Modifier.padding(end = 4.dp).size(8.dp).clip(CircleShape).background(Color.Red))
        }
    }
}

@Composable
fun TaskBlock(task: Task, modifier: Modifier = Modifier) {
    val bgColor = try { Color(android.graphics.Color.parseColor(task.colorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
    val textColor = getContrastColor(task.colorHex)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor.copy(alpha = 0.9f))
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(4.dp)
    ) {
        Text(
            text = task.title,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        task.description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = textColor.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun NewEventPlaceholderBlock(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)) // Un poco más visible
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "+ Nuevo evento",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Medium
        )
    }
}

// Cálculo de posición en la pantalla
fun calculateTaskPosition(start: LocalDateTime, end: LocalDateTime): Pair<Dp, Dp> {
    val startMinutes = start.hour * 60 + start.minute
    val endMinutes = end.hour * 60 + end.minute
    // Aseguramos que al menos se vea un poco si dura 0, pero en tu caso durará 60 min
    val duration = (endMinutes - startMinutes).coerceAtLeast(15)

    val topOffset = HOUR_HEIGHT * (startMinutes / 60f)
    val height = HOUR_HEIGHT * (duration / 60f)
    return topOffset to height
}