package com.example.app_de_gestion_de_horarios_movil.ui.components

import android.graphics.Color.parseColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun StripCalendar(
    selectedDate: LocalDate,
    eventsColors: Map<LocalDate, List<String>>,
    startDate: LocalDate,
    endDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. CÁLCULO DE SEMANAS
    // Calculamos cuántos días totales hay y dividimos entre 7 para saber cuántas "páginas" necesitamos.
    val totalDays = remember(startDate, endDate) {
        ChronoUnit.DAYS.between(
            startDate.toJavaLocalDate(),
            endDate.toJavaLocalDate()
        ).toInt() + 1
    }

    // El número de páginas será el total de días / 7 (redondeando hacia arriba)
    val pageCount = (totalDays + 6) / 7

    // 2. ESTADO DEL PAGER
    // Calculamos en qué página debe iniciar basándonos en la fecha seleccionada
    val initialPage = remember(startDate, selectedDate) {
        val daysDiff = ChronoUnit.DAYS.between(
            startDate.toJavaLocalDate(),
            selectedDate.toJavaLocalDate()
        ).toInt()
        (daysDiff / 7).coerceIn(0, pageCount - 1)
    }

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { pageCount }
    )

    // 3. TÍTULO DINÁMICO (Basado en la página actual del Pager)
    val visibleMonthTitle by remember {
        derivedStateOf {
            // Calculamos el primer día de la semana que se está viendo
            val daysToAdd = pagerState.currentPage * 7
            val currentWeekDate = startDate.plus(DatePeriod(days = daysToAdd))

            // Formateamos el título
            "${currentWeekDate.month.name.lowercase().replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }} ${currentWeekDate.year}"
        }
    }

    // Sincronización inversa: Si seleccionas una fecha (ej: desde un DatePicker externo),
    // movemos el pager a esa semana.
    LaunchedEffect(selectedDate) {
        val daysDiff = ChronoUnit.DAYS.between(
            startDate.toJavaLocalDate(),
            selectedDate.toJavaLocalDate()
        ).toInt()
        val targetPage = (daysDiff / 7)

        if (pagerState.currentPage != targetPage && targetPage in 0 until pageCount) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {

            // Título del Mes
            Text(
                text = visibleMonthTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            // 4. EL COMPONENTE DE PAGINACIÓN
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp), // Margen lateral
                pageSpacing = 8.dp // Espacio entre semanas
            ) { pageIndex ->

                // Renderizamos UNA SEMANA (Fila de 7 días)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween // Distribuye los 7 días equitativamente
                ) {
                    // Calculamos los 7 días de ESTA página específica
                    val startOfWeekDays = pageIndex * 7

                    for (i in 0 until 7) {
                        val date = startDate.plus(DatePeriod(days = startOfWeekDays + i))

                        // Protección por si el último grupo tiene menos de 7 días (al final del rango global)
                        if (date > endDate) {
                            // Renderizamos un espacio vacío para mantener la alineación
                            Spacer(modifier = Modifier.width(52.dp))
                        } else {
                            val dayColors = eventsColors[date] ?: emptyList()

                            CalendarDayItem(
                                date = date,
                                isSelected = date == selectedDate,
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
    eventColors: List<String>, // Lista de Hex Codes
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    )
    val contentColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(52.dp) // Un poco más ancho para que quepan 4 puntos
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        // Día Semana
        Text(
            text = date.dayOfWeek.name.take(3), // "Mon", "Tue" (3 letras es mejor)
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = 0.7f),
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Número Día
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )

        Spacer(modifier = Modifier.height(6.dp))

        // --- FILA DE PUNTITOS DE COLORES ---
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.height(6.dp) // Altura fija para evitar saltos
        ) {
            // Tomamos máximo 4 para no saturar
            eventColors.take(4).forEach { hexCode ->
                val dotColor = remember(hexCode) {
                    try { Color(parseColor(hexCode)) } catch (e: Exception) { Color.Gray }
                }

                // Si el día está seleccionado (Fondo Azul), forzamos los puntos a Blanco
                // para que se vean bien. Si no, usamos su color original.
                val finalColor = if(isSelected) Color.White.copy(alpha = 0.8f) else dotColor

                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(finalColor)
                )
            }
        }
    }
}