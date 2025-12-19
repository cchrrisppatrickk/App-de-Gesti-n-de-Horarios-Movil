package com.example.app_de_gestion_de_horarios_movil.ui.components

import android.graphics.Color.parseColor
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StripCalendar(
    selectedDate: LocalDate,
    eventsColors: Map<LocalDate, List<String>>,
    startDate: LocalDate,
    endDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    // Variables de ajuste visual
    headerPadding: PaddingValues = PaddingValues(start = 24.dp, end = 24.dp, top = 5.dp, bottom = 8.dp),
    monthTextSize: TextUnit = 25.sp,
    yearTextSize: TextUnit = 18.sp
) {
    val today = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    val totalDays = remember(startDate, endDate) {
        ChronoUnit.DAYS.between(startDate.toJavaLocalDate(), endDate.toJavaLocalDate()).toInt() + 1
    }
    val pageCount = (totalDays + 6) / 7

    val initialPage = remember(startDate, selectedDate) {
        val daysDiff = ChronoUnit.DAYS.between(startDate.toJavaLocalDate(), selectedDate.toJavaLocalDate()).toInt()
        (daysDiff / 7).coerceIn(0, pageCount - 1)
    }

    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { pageCount })

    val currentMonthName by remember {
        derivedStateOf {
            val daysToAdd = pagerState.currentPage * 7
            val date = startDate.plus(DatePeriod(days = daysToAdd))
            date.month.name.lowercase().replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }
    }

    val currentYear by remember {
        derivedStateOf {
            val daysToAdd = pagerState.currentPage * 7
            startDate.plus(DatePeriod(days = daysToAdd)).year.toString()
        }
    }

    LaunchedEffect(selectedDate) {
        val daysDiff = ChronoUnit.DAYS.between(startDate.toJavaLocalDate(), selectedDate.toJavaLocalDate()).toInt()
        val targetPage = (daysDiff / 7)
        if (pagerState.currentPage != targetPage && targetPage in 0 until pageCount) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        // CORRECCIÓN: Usamos 'surface' (#252525) en lugar de 'background' (#161616)
        // para recuperar el tono gris de tu paleta.
        color = MaterialTheme.colorScheme.surface,
        // Agregamos esquinas redondeadas abajo para que se vea elegante sobre el fondo negro
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        shadowElevation = 4.dp // Un poco de sombra para separar del fondo negro
    ) {
        Column(modifier = Modifier.padding(bottom = 16.dp)) {

            // Cabecera (Mes y Año)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(headerPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentMonthName,
                    fontSize = monthTextSize,
                    color = MaterialTheme.colorScheme.primary, // Rojo Coral
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = currentYear,
                    fontSize = yearTextSize,
                    color = MaterialTheme.colorScheme.onSurface, // Blanco/Gris claro (OnSurface)
                    fontWeight = FontWeight.Bold
                )
            }

            // Pager de Días
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                pageSpacing = 8.dp
            ) { pageIndex ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val startOfWeekDays = pageIndex * 7

                    for (i in 0 until 7) {
                        val date = startDate.plus(DatePeriod(days = startOfWeekDays + i))

                        if (date > endDate) {
                            Spacer(modifier = Modifier.width(52.dp))
                        } else {
                            val dayColors = eventsColors[date] ?: emptyList()

                            CalendarDayItem(
                                date = date,
                                isSelected = date == selectedDate,
                                isToday = date == today,
                                eventColors = dayColors,
                                onClick = { onDateSelected(date) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayItem(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    eventColors: List<String>,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, label = "bg"
    )

    val contentColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        label = "content"
    )

    // Borde solo si es HOY y no está seleccionado
    val borderModifier = if (isToday && !isSelected) {
        Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            shape = RoundedCornerShape(12.dp)
        )
    } else {
        Modifier
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(borderModifier)
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 10.dp)
    ) {
        // Día Semana
        Text(
            text = date.dayOfWeek.name.take(3),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Normal,
            color = contentColor.copy(alpha = 0.5f),
            maxLines = 1,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Número Día
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Puntos
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.height(6.dp)
        ) {
            eventColors.take(3).forEach { hexCode ->
                val dotColor = remember(hexCode) {
                    try { Color(parseColor(hexCode)) } catch (e: Exception) { Color.Gray }
                }
                val finalColor = if(isSelected) Color.White.copy(alpha = 0.9f) else dotColor

                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(finalColor)
                )
            }
        }
    }
}