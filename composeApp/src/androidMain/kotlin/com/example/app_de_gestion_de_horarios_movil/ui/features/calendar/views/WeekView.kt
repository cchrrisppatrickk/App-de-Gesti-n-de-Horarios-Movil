package com.example.app_de_gestion_de_horarios_movil.ui.features.calendar.views

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.ui.components.plusMinutes
import com.example.app_de_gestion_de_horarios_movil.ui.components.toUiString
import kotlinx.datetime.*
import kotlin.math.roundToInt

// --- CONFIGURACIÓN DE DISEÑO ---
private val HOUR_HEIGHT = 60.dp
private val TIME_COLUMN_WIDTH = 55.dp
private const val START_PAGE_INDEX = Int.MAX_VALUE / 2

data class GhostEventState(
    val dayDate: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val topOffset: Dp,
    val height: Dp,
    val colIndex: Int
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeekView(
    selectedDate: LocalDate,
    tasks: Map<LocalDate, List<Task>>,
    onDateSelected: (LocalDate) -> Unit,
    onTaskClick: (Task) -> Unit,
    onRangeSelected: (LocalDate, LocalTime, LocalTime) -> Unit
) {
    val anchorDate = remember { selectedDate }
    val anchorWeekStart = remember(anchorDate) { getStartOfWeek(anchorDate) }

    val initialPage = remember { START_PAGE_INDEX }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { Int.MAX_VALUE })
    val verticalScrollState = rememberScrollState()

    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current

    LaunchedEffect(pagerState.currentPage) {
        val diffWeeks = pagerState.currentPage - initialPage
        val newWeekStart = anchorWeekStart.plus(DatePeriod(days = diffWeeks * 7))
        if (getStartOfWeek(selectedDate) != newWeekStart) {
            onDateSelected(newWeekStart)
        }
    }

    LaunchedEffect(selectedDate) {
        val targetWeekStart = getStartOfWeek(selectedDate)
        val weeksDiff = targetWeekStart.minus(anchorWeekStart).days / 7
        val targetPage = initialPage + weeksDiff
        if (pagerState.currentPage != targetPage) {
            pagerState.scrollToPage(targetPage)
        }
    }

    var ghostEvent by remember { mutableStateOf<GhostEventState?>(null) }
    var columnWidthPx by remember { mutableFloatStateOf(0f) }

    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
        val diffWeeks = page - initialPage
        val pageWeekStart = anchorWeekStart.plus(DatePeriod(days = diffWeeks * 7))
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        Column(modifier = Modifier.fillMaxSize()) {

            // HEADER FIJO
            WeekHeaderRow(
                startOfWeek = pageWeekStart,
                today = today,
                selectedDate = selectedDate
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // CUERPO SCROLLABLE
            Box(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(verticalScrollState)
                    // GESTOR DE TOQUES Y ARRASTRES
                    .pointerInput(Unit) {
                        // 1. ARRASTRE (Long Press + Drag)
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)

                                val x = offset.x - with(density) { TIME_COLUMN_WIDTH.toPx() }
                                if (x > 0 && columnWidthPx > 0) {
                                    val colIndex = (x / columnWidthPx).toInt().coerceIn(0, 6)
                                    val dayDate = pageWeekStart.plus(DatePeriod(days = colIndex))

                                    // HORA EXACTA: Eliminar decimales para seleccionar la hora completa (00 min)
                                    val hourHeightPx = with(density) { HOUR_HEIGHT.toPx() }
                                    val startHour = (offset.y / hourHeightPx).toInt().coerceIn(0, 23)

                                    val startTime = LocalTime(startHour, 0)
                                    val endTime = startTime.plusMinutes(60) // Duración base 1h

                                    val (top, height) = calculateTaskPosition(startTime, endTime)
                                    ghostEvent = GhostEventState(dayDate, startTime, endTime, top, height, colIndex)
                                }
                            },
                            onDrag = { change, _ ->
                                ghostEvent?.let { current ->
                                    // Al arrastrar el final, permitimos intervalos de 15 min para flexibilidad
                                    val totalMinutes = (change.position.y / with(density) { HOUR_HEIGHT.toPx() }) * 60
                                    val snappedMinutes = (totalMinutes / 15).roundToInt() * 15

                                    val startMinVal = current.startTime.hour * 60 + current.startTime.minute
                                    val validEndMinutes = snappedMinutes.coerceAtLeast(startMinVal + 15)

                                    val endHour = (validEndMinutes / 60).coerceIn(0, 23)
                                    val endMin = validEndMinutes % 60
                                    val newEndTime = LocalTime(endHour, endMin)

                                    val (_, newHeight) = calculateTaskPosition(current.startTime, newEndTime)
                                    ghostEvent = current.copy(endTime = newEndTime, height = newHeight)
                                }
                            },
                            onDragEnd = {
                                ghostEvent?.let {
                                    onRangeSelected(it.dayDate, it.startTime, it.endTime)
                                    ghostEvent = null
                                }
                            },
                            onDragCancel = { ghostEvent = null }
                        )
                    }
                    .pointerInput(Unit) {
                        // 2. CLICK SIMPLE (Tap)
                        detectTapGestures { offset ->
                            val x = offset.x - with(density) { TIME_COLUMN_WIDTH.toPx() }
                            if (x > 0 && columnWidthPx > 0) {
                                val colIndex = (x / columnWidthPx).toInt().coerceIn(0, 6)
                                val dayDate = pageWeekStart.plus(DatePeriod(days = colIndex))

                                // HORA EXACTA: Selecciona el bloque completo donde se hizo click
                                val hourHeightPx = with(density) { HOUR_HEIGHT.toPx() }
                                val clickedHour = (offset.y / hourHeightPx).toInt().coerceIn(0, 23)

                                val start = LocalTime(clickedHour, 0)
                                val end = start.plusMinutes(60)

                                // Efecto visual inmediato (opcional, aquí enviamos directo al callback)
                                onRangeSelected(dayDate, start, end)
                            }
                        }
                    }
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    TimeSidebarColumn()

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned { coordinates ->
                                columnWidthPx = coordinates.size.width / 7f
                            }
                    ) {
                        for (i in 0 until 7) {
                            val dayDate = pageWeekStart.plus(DatePeriod(days = i))
                            val dayTasks = tasks[dayDate] ?: emptyList()
                            val isDragCol = ghostEvent?.colIndex == i

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(HOUR_HEIGHT * 24)
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                            ) {
                                // Líneas horizontales
                                for (h in 0..23) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(top = HOUR_HEIGHT * h),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                    )
                                }

                                // Tareas Existentes
                                dayTasks.forEach { task ->
                                    if (!task.isAllDay) {
                                        val (top, height) = calculateTaskPosition(task.startTime.time, task.endTime.time)
                                        TaskBlock(
                                            task = task,
                                            modifier = Modifier
                                                .padding(horizontal = 1.dp)
                                                .fillMaxWidth()
                                                .offset(y = top)
                                                .height(height)
                                                .clickable { onTaskClick(task) }
                                        )
                                    }
                                }

                                // GHOST EVENT BOX (Visualización de creación)
                                if (isDragCol && ghostEvent != null) {
                                    GhostEventBox(ghostEvent!!)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- COMPONENTE GHOST EVENT BOX (Estilo "+ Nuevo evento") ---
@Composable
fun GhostEventBox(ghost: GhostEventState) {
    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .fillMaxWidth()
            .offset(y = ghost.topOffset)
            .height(ghost.height)
            .clip(RoundedCornerShape(4.dp))
            // Fondo azul claro semitransparente (estilo Material 3 Container)
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f))
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
            .zIndex(20f) // Aseguramos que esté por encima de todo
            .padding(4.dp),
        contentAlignment = Alignment.TopStart // Alineado arriba a la izquierda como pediste
    ) {
        Column {
            Text(
                text = "+ Nuevo evento",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // Mostrar la hora solo si el bloque es suficientemente alto (ej. > 30 min)
            if (ghost.height > 25.dp) {
                Text(
                    text = "${ghost.startTime.toUiString()} - ${ghost.endTime.toUiString()}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun TimeSidebarColumn() {
    Column(
        modifier = Modifier
            .width(TIME_COLUMN_WIDTH)
            .height(HOUR_HEIGHT * 24)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        for (h in 0..23) {
            Box(
                modifier = Modifier
                    .height(HOUR_HEIGHT)
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                val hourText = when {
                    h == 0 -> ""
                    h < 12 -> "$h AM"
                    h == 12 -> "12 PM"
                    else -> "${h - 12} PM"
                }

                Text(
                    text = hourText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.offset(y = (-7).dp)
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeekHeaderRow(startOfWeek: LocalDate, today: LocalDate, selectedDate: LocalDate) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = TIME_COLUMN_WIDTH)
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 8.dp)
    ) {
        val days = listOf("DOM", "LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB")
        for (i in 0 until 7) {
            val date = startOfWeek.plus(DatePeriod(days = i))
            val isToday = date == today
            val isSelected = date == selectedDate

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = days[i],
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if(isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isToday -> MaterialTheme.colorScheme.primary
                                isSelected -> MaterialTheme.colorScheme.secondaryContainer
                                else -> Color.Transparent
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

// Helpers
@RequiresApi(Build.VERSION_CODES.O)
fun getStartOfWeek(date: LocalDate): LocalDate {
    val currentDayOfWeek = date.dayOfWeek.isoDayNumber
    val daysToSubtract = if (currentDayOfWeek == 7) 0 else currentDayOfWeek
    return date.minus(DatePeriod(days = daysToSubtract))
}

fun LocalDate.minus(other: LocalDate): DatePeriod {
    return DatePeriod(days = (this.toEpochDays() - other.toEpochDays()))
}

fun calculateTaskPosition(start: LocalTime, end: LocalTime): Pair<Dp, Dp> {
    val startMin = start.hour * 60 + start.minute
    val endMin = if (end.hour == 0 && end.minute == 0) 24 * 60 else end.hour * 60 + end.minute
    val duration = (endMin - startMin).coerceAtLeast(15)

    val top = HOUR_HEIGHT * (startMin / 60f)
    val height = HOUR_HEIGHT * (duration / 60f)
    return top to height
}

