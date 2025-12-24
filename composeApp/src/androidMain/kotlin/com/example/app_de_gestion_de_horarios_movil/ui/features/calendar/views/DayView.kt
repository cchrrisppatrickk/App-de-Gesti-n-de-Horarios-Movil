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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.ui.features.calendar.getContrastColor
import kotlinx.coroutines.delay
import kotlinx.datetime.*
import java.time.format.DateTimeFormatter
import java.util.Locale

// Configuración de dimensiones
private val HOUR_HEIGHT = 60.dp
private val TIME_COLUMN_WIDTH = 50.dp
private const val START_PAGE_INDEX = Int.MAX_VALUE / 2

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DayView(
    selectedDate: LocalDate, // Fecha seleccionada actualmente
    tasksMap: Map<LocalDate, List<Task>>, // Mapa completo para poder scrollear y ver otros días
    onDateChange: (LocalDate) -> Unit, // Callback al deslizar
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Configuración del Pager para scroll infinito de días
    val initialPage = remember { START_PAGE_INDEX }
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { Int.MAX_VALUE })

    // Fecha base para cálculos (Hoy)
    val baseDate = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }

    // Sincronización Pager -> Fecha (Al deslizar)
    LaunchedEffect(pagerState.currentPage) {
        val diffDays = pagerState.currentPage - initialPage
        val newDate = baseDate.plus(DatePeriod(days = diffDays))
        if (newDate != selectedDate) {
            onDateChange(newDate)
        }
    }

    // Sincronización Fecha -> Pager (Al seleccionar fecha en picker)
    LaunchedEffect(selectedDate) {
        val daysDiff = selectedDate.toEpochDays() - baseDate.toEpochDays()
        val targetPage = initialPage + daysDiff
        if (pagerState.currentPage != targetPage) {
            pagerState.scrollToPage(targetPage)
        }
    }

    // Estado para la hora actual (Línea de tiempo)
    var currentTime by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())) }

    // Timer para actualizar la línea roja cada minuto
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            delay(60_000L) // Esperar 1 minuto
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier
    ) { page ->
        val diffDays = page - initialPage
        val pageDate = baseDate.plus(DatePeriod(days = diffDays))
        val tasksForDay = tasksMap[pageDate] ?: emptyList()
        val isToday = pageDate == currentTime.date

        Column(modifier = Modifier.verticalScroll(scrollState)) {
            // Dibujamos las 24 horas del día
            Box(modifier = Modifier.fillMaxWidth().height(HOUR_HEIGHT * 24)) {

                // 1. LÍNEAS DE FONDO Y HORAS
                for (hour in 0..23) {
                    val topOffset = HOUR_HEIGHT * hour

                    // Hora (Columna Izquierda)
                    Text(
                        text = String.format("%02d:00", hour),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .width(TIME_COLUMN_WIDTH)
                            .padding(top = topOffset + 4.dp, end = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )

                    // Línea divisoria
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(start = TIME_COLUMN_WIDTH, top = topOffset)
                            .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }

                // 2. BLOQUES DE TAREAS (EVENTS)
                tasksForDay.forEach { task ->
                    if (!task.isAllDay) {
                        val (topDp, heightDp) = calculateTaskPosition(task.startTime, task.endTime)

                        TaskBlock(
                            task = task,
                            modifier = Modifier
                                .padding(start = TIME_COLUMN_WIDTH + 4.dp, end = 4.dp)
                                .fillMaxWidth()
                                .offset(y = topDp)
                                .height(heightDp)
                                .clickable { onTaskClick(task) }
                        )
                    }
                }

                // 3. LÍNEA DE TIEMPO "AHORA" (Solo si es hoy)
                if (isToday) {
                    CurrentTimeIndicator(
                        time = currentTime.time,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun CurrentTimeIndicator(
    time: kotlinx.datetime.LocalTime,
    modifier: Modifier = Modifier
) {
    // Calcular posición Y
    val minutes = time.hour * 60 + time.minute
    val topOffset = HOUR_HEIGHT * (minutes / 60f)

    Box(
        modifier = modifier
            .offset(y = topOffset)
            .height(2.dp) // Altura del contenedor de la línea
    ) {
        // Línea roja
        HorizontalDivider(
            modifier = Modifier
                .padding(start = TIME_COLUMN_WIDTH)
                .fillMaxWidth()
                .align(Alignment.Center),
            color = Color.Red,
            thickness = 1.dp
        )

        // Círculo rojo en la columna de tiempo
        Box(
            modifier = Modifier
                .width(TIME_COLUMN_WIDTH)
                .align(Alignment.CenterStart),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 4.dp) // Pegado a la línea divisoria
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
            )
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
            maxLines = 1
        )
        task.description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = textColor.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

// LÓGICA MATEMÁTICA
fun calculateTaskPosition(start: LocalDateTime, end: LocalDateTime): Pair<Dp, Dp> {
    val startMinutes = start.hour * 60 + start.minute
    val endMinutes = end.hour * 60 + end.minute
    val duration = (endMinutes - startMinutes).coerceAtLeast(15)

    val topOffset = HOUR_HEIGHT * (startMinutes / 60f)
    val height = HOUR_HEIGHT * (duration / 60f)

    return topOffset to height
}