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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import kotlinx.datetime.*
import java.time.format.TextStyle
import java.util.Locale

// Configuración visual
private val HOUR_HEIGHT = 60.dp
private val TIME_COLUMN_WIDTH = 40.dp
private const val START_PAGE_INDEX = Int.MAX_VALUE / 2

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeekView(
    selectedDate: LocalDate,
    tasks: Map<LocalDate, List<Task>>, // Recibimos todo el mapa para buscar los días
    onDateSelected: (LocalDate) -> Unit, // Para actualizar la fecha al deslizar semana
    onTaskClick: (Task) -> Unit
) {
    // 1. Configurar Pager para semanas infinitas
    val initialPage = remember { START_PAGE_INDEX }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { Int.MAX_VALUE })

    // Scroll vertical compartido para toda la semana
    val verticalScrollState = rememberScrollState()

    // Sincronización Pager -> Fecha
    LaunchedEffect(pagerState.currentPage) {
        val diffWeeks = pagerState.currentPage - initialPage
        // Calculamos el inicio de la semana de la fecha seleccionada
        val currentWeekStart = getStartOfWeek(selectedDate)
        val newWeekStart = currentWeekStart.plus(DatePeriod(days = diffWeeks * 7))

        // Si la semana cambió visualmente, actualizamos la fecha seleccionada al lunes de esa semana
        if (getStartOfWeek(selectedDate) != newWeekStart) {
            onDateSelected(newWeekStart)
        }
    }

    // Sincronización Fecha -> Pager (Si cambias fecha desde el picker)
    LaunchedEffect(selectedDate) {
        // Lógica inversa: calcular página basada en fecha (simplificada para este ejemplo)
        // Nota: Para una implementación perfecta, necesitarías una "fecha base" inmutable como en el calendario mensual.
    }

    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
        val diffWeeks = page - initialPage
        // Esta fecha base debe ser consistente. Usamos la fecha actual del sistema o la seleccionada al inicio.
        // Para simplificar, calculamos respecto a la fecha seleccionada actual pero ajustada por páginas.
        // TRUCO: Usar una fecha fija base (ej. hoy) funciona mejor, similar al CalendarScreen.
        val baseDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val baseWeekStart = getStartOfWeek(baseDate)
        val pageWeekStart = baseWeekStart.plus(DatePeriod(days = diffWeeks * 7))

        Column(modifier = Modifier.fillMaxSize()) {

            // A. ENCABEZADO DE LA SEMANA (Dómingo 23, Lunes 24...)
            WeekHeaderRow(
                startOfWeek = pageWeekStart,
                today = baseDate,
                selectedDate = selectedDate
            )

            // B. CUERPO DE LA SEMANA (Scroll Vertical)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScrollState) // <--- SCROLL COMPARTIDO
            ) {
                // COLUMNA 1: HORAS
                TimeSidebarColumn()

                // COLUMNAS 2-8: DÍAS
                for (i in 0 until 7) {
                    val dayDate = pageWeekStart.plus(DatePeriod(days = i))
                    val dayTasks = tasks[dayDate] ?: emptyList()

                    Box(
                        modifier = Modifier
                            .weight(1f) // Cada día ocupa el mismo ancho
                            .height(HOUR_HEIGHT * 24)
                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    ) {
                        // Líneas horizontales de fondo
                        for (h in 0..23) {
                            HorizontalDivider(
                                modifier = Modifier.padding(top = HOUR_HEIGHT * h).fillMaxWidth(),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                            )
                        }

                        // Eventos
                        dayTasks.forEach { task ->
                            if (!task.isAllDay) {
                                val (topDp, heightDp) = calculateTaskPosition(task.startTime, task.endTime)
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
            .padding(start = TIME_COLUMN_WIDTH) // Dejar espacio para la columna de horas
            .padding(vertical = 8.dp)
    ) {
        val days = listOf("D", "L", "M", "M", "J", "V", "S") // Ajusta según tu Locale
        for (i in 0 until 7) {
            val date = startOfWeek.plus(DatePeriod(days = i))
            val isToday = date == today

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = days[i],
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Número del día con círculo si es hoy
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent),
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

// Helper para obtener inicio de semana (Domingo)
@RequiresApi(Build.VERSION_CODES.O)
fun getStartOfWeek(date: LocalDate): LocalDate {
    // Kotlin ISO: Monday = 1, Sunday = 7.
    // Si queremos que la semana empiece en Domingo:
    val currentDayOfWeek = date.dayOfWeek.isoDayNumber // 1..7
    // Si es Domingo (7), restamos 0. Si es Lunes (1), restamos 1.
    val daysToSubtract = if (currentDayOfWeek == 7) 0 else currentDayOfWeek
    return date.minus(DatePeriod(days = daysToSubtract))
}