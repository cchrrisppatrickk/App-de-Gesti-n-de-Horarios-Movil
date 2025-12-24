package com.example.app_de_gestion_de_horarios_movil.ui.features.calendar.views

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import kotlinx.datetime.*

// Configuración visual
private val HOUR_HEIGHT = 60.dp
private val TIME_COLUMN_WIDTH = 40.dp
private const val START_PAGE_INDEX = Int.MAX_VALUE / 2

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeekView(
    selectedDate: LocalDate,
    tasks: Map<LocalDate, List<Task>>,
    onDateSelected: (LocalDate) -> Unit,
    onTaskClick: (Task) -> Unit
) {
    // 1. OPTIMIZACIÓN: Fecha Ancla.
    // Usamos la fecha con la que se abrió la vista como punto de referencia fijo (Página Central).
    // Esto evita saltos al abrir el calendario en una fecha futura.
    val anchorDate = remember { selectedDate }
    val anchorWeekStart = remember(anchorDate) { getStartOfWeek(anchorDate) }

    val initialPage = remember { START_PAGE_INDEX }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { Int.MAX_VALUE })

    // Scroll vertical compartido (Sticky Header funciona gracias a la estructura Column fuera del Row scrollable)
    val verticalScrollState = rememberScrollState()

    // 2. Sincronización Pager -> Fecha (Swipe del usuario)
    LaunchedEffect(pagerState.currentPage) {
        val diffWeeks = pagerState.currentPage - initialPage
        val newWeekStart = anchorWeekStart.plus(DatePeriod(days = diffWeeks * 7))

        // Solo actualizamos si la semana visible es diferente a la de la fecha seleccionada actual
        if (getStartOfWeek(selectedDate) != newWeekStart) {
            onDateSelected(newWeekStart)
        }
    }

    // 3. Sincronización Fecha -> Pager (Cambio externo, ej: DatePicker)
    // Esta es la lógica inversa que faltaba en tu código base
    LaunchedEffect(selectedDate) {
        val targetWeekStart = getStartOfWeek(selectedDate)
        // Calculamos cuántas semanas hay de diferencia entre la fecha ancla y la nueva fecha
        val weeksDiff = targetWeekStart.minus(anchorWeekStart).days / 7
        val targetPage = initialPage + weeksDiff

        if (pagerState.currentPage != targetPage) {
            pagerState.scrollToPage(targetPage)
        }
    }

    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
        // Calculamos la fecha de ESTA página basándonos en la fecha ancla estable
        val diffWeeks = page - initialPage
        val pageWeekStart = anchorWeekStart.plus(DatePeriod(days = diffWeeks * 7))

        // Fecha actual para resaltar el "Hoy"
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        Column(modifier = Modifier.fillMaxSize()) {

            // A. ENCABEZADO FIJO (Sticky Header)
            // Al estar fuera del verticalScroll, no se mueve al bajar por las horas.
            WeekHeaderRow(
                startOfWeek = pageWeekStart,
                today = today,
                selectedDate = selectedDate
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // B. CUERPO DE LA SEMANA (Scroll Vertical)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScrollState) // El scroll solo afecta a este Row
            ) {
                // COLUMNA 1: HORAS
                TimeSidebarColumn()

                // COLUMNAS 2-8: DÍAS Y EVENTOS
                Row(modifier = Modifier.weight(1f)) {
                    for (i in 0 until 7) {
                        val dayDate = pageWeekStart.plus(DatePeriod(days = i))
                        val dayTasks = tasks[dayDate] ?: emptyList()

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(HOUR_HEIGHT * 24)
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        ) {
                            // Líneas horizontales de fondo
                            for (h in 0..23) {
                                HorizontalDivider(
                                    modifier = Modifier
                                        .padding(top = HOUR_HEIGHT * h)
                                        .fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                )
                            }

                            // Renderizado de Tareas
                            dayTasks.forEach { task ->
                                if (!task.isAllDay) {
                                    val (topDp, heightDp) = calculateTaskPosition(task.startTime.time, task.endTime.time)
                                    TaskBlock(
                                        task = task,
                                        modifier = Modifier
                                            .padding(horizontal = 1.dp)
                                            .fillMaxWidth()
                                            .offset(y = topDp)
                                            .height(heightDp)
                                            .clickable { onTaskClick(task) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeekHeaderRow(
    startOfWeek: LocalDate,
    today: LocalDate,
    selectedDate: LocalDate
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = TIME_COLUMN_WIDTH)
            .padding(vertical = 8.dp)
    ) {
        val days = listOf("D", "L", "M", "M", "J", "V", "S")
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
                    style = MaterialTheme.typography.labelSmall,
                    color = if(isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .size(28.dp)
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
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
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
    ) {
        for (hour in 0..23) {
            Box(
                modifier = Modifier.height(HOUR_HEIGHT),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = String.format("%02d", hour),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

// --- HELPERS MATEMÁTICOS ---

@RequiresApi(Build.VERSION_CODES.O)
fun getStartOfWeek(date: LocalDate): LocalDate {
    val currentDayOfWeek = date.dayOfWeek.isoDayNumber
    val daysToSubtract = if (currentDayOfWeek == 7) 0 else currentDayOfWeek
    return date.minus(DatePeriod(days = daysToSubtract))
}

// Extensión necesaria para calcular diferencia de días en DatePeriod de forma simple
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

