package com.example.app_de_gestion_de_horarios_movil.ui.features.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_de_gestion_de_horarios_movil.ui.components.*
import com.example.app_de_gestion_de_horarios_movil.ui.features.create_task.CreateTaskSheet
import kotlinx.datetime.toJavaLocalDateTime
import org.koin.androidx.compose.koinViewModel
import java.time.temporal.ChronoUnit
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onNavigateToWizard: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTask by viewModel.selectedTask.collectAsStateWithLifecycle()
    val calendarColors by viewModel.calendarColors.collectAsStateWithLifecycle()

    var isGroupEditMode by remember { mutableStateOf(false) }
    var showCreateTaskSheet by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    // Estado local para guardar los datos del hueco seleccionado temporalmente
    var selectedGapStart by remember { mutableStateOf<LocalTime?>(null) }
    var selectedGapEnd by remember { mutableStateOf<LocalTime?>(null) }

    // --- CONFIGURACIÓN DEL PAGER ---
    val totalDays = remember(viewModel.calendarStartDate, viewModel.calendarEndDate) {
        ChronoUnit.DAYS.between(
            viewModel.calendarStartDate.toJavaLocalDate(),
            viewModel.calendarEndDate.toJavaLocalDate()
        ).toInt() + 1
    }

    val initialPage = remember(viewModel.calendarStartDate) {
        ChronoUnit.DAYS.between(
            viewModel.calendarStartDate.toJavaLocalDate(),
            uiState.selectedDate.toJavaLocalDate()
        ).toInt()
    }

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { totalDays }
    )

    // --- SINCRONIZACIÓN ---
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
    var isProgrammaticScroll by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.selectedDate) {
        val targetPage = ChronoUnit.DAYS.between(
            viewModel.calendarStartDate.toJavaLocalDate(),
            uiState.selectedDate.toJavaLocalDate()
        ).toInt()

        if (pagerState.currentPage != targetPage && targetPage in 0 until totalDays) {
            isProgrammaticScroll = true
            pagerState.animateScrollToPage(targetPage)
            isProgrammaticScroll = false
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (!isProgrammaticScroll || isDragged) {
                val newDate = viewModel.calendarStartDate.plus(DatePeriod(days = page))
                if (newDate != uiState.selectedDate) {
                    viewModel.onDateSelected(newDate)
                }
            }
        }
    }

    // --- UI PRINCIPAL ---
    Scaffold(
        // Fondo Matte Oscuro (BackgroundBlack #161616)
        containerColor = MaterialTheme.colorScheme.background,

        // Edge-to-Edge: Sin insets para que el contenido suba
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),

        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateTaskSheet = true },
                // Fondo Coral (Primary)
                containerColor = MaterialTheme.colorScheme.primary,
                // Icono Blanco
                contentColor = Color.White,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Tarea")
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Calendario Superior (Gris Oscuro #252525 gracias a su Surface interno)
            StripCalendar(
                selectedDate = uiState.selectedDate,
                eventsColors = calendarColors,
                startDate = viewModel.calendarStartDate,
                endDate = viewModel.calendarEndDate,
                onDateSelected = { newDate -> viewModel.onDateSelected(newDate) },
                modifier = Modifier.fillMaxWidth()
            )

            // NOTA: Eliminamos el HorizontalDivider aquí porque el calendario ya tiene
            // su propia forma y color (Surface) que lo separa del fondo negro.

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalAlignment = Alignment.Top,
                pageSpacing = 16.dp
            ) { pageIndex ->

                val pageDate = viewModel.calendarStartDate.plus(DatePeriod(days = pageIndex))

                if (pageDate == uiState.selectedDate) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when {
                            uiState.isLoading -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            uiState.tasks.isEmpty() -> {
                                EmptyStateMessage(modifier = Modifier.align(Alignment.Center))
                            }
                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 80.dp, top = 16.dp)
                                ) {
                                    itemsIndexed(
                                        items = uiState.timelineItems,
                                        // MEJORA DE RENDIMIENTO: Keys estables
                                        key = { _, item ->
                                            when (item) {
                                                is TimelineItem.TaskItem -> item.task.id ?: item.hashCode()
                                                is TimelineItem.GapItem -> "${item.start}-${item.end}"
                                            }
                                        },
                                        // MEJORA DE RENDIMIENTO: ContentType
                                        contentType = { _, item ->
                                            when (item) {
                                                is TimelineItem.TaskItem -> "task"
                                                is TimelineItem.GapItem -> "gap"
                                            }
                                        }
                                    ) { index, item ->
                                        when (item) {
                                            is TimelineItem.TaskItem -> {
                                                TimelineTaskRow(
                                                    task = item.task,
                                                    isFirst = index == 0,
                                                    isLast = index == uiState.timelineItems.lastIndex,
                                                    onToggleCompletion = { viewModel.onToggleCompletion(item.task) },
                                                    modifier = Modifier
                                                        .padding(horizontal = 16.dp)
                                                        .clickable { viewModel.onTaskSelected(item.task) }
                                                        .animateItem() // Animación suave (Compose 1.7+)
                                                )
                                            }

                                            is TimelineItem.GapItem -> {
                                                TimelineGapRow(
                                                    startTime = item.start,
                                                    durationMinutes = item.durationMinutes,
                                                    onClick = {
                                                        val javaStart = item.start.toLocalTime()
                                                        val javaEnd = item.end.toLocalTime()
                                                        selectedGapStart = LocalTime(javaStart.hour, javaStart.minute)
                                                        selectedGapEnd = LocalTime(javaEnd.hour, javaEnd.minute)
                                                        showCreateTaskSheet = true
                                                    },
                                                    modifier = Modifier.padding(horizontal = 16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Placeholder mientras deslizas
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }

    // --- SHEETS ---
    if (showCreateTaskSheet) {
        CreateTaskSheet(
            taskToEdit = taskToEdit,
            isGroupEdit = isGroupEditMode,
            initialStartTime = selectedGapStart,
            initialEndTime = selectedGapEnd,
            onDismiss = {
                showCreateTaskSheet = false
                taskToEdit = null
                selectedGapStart = null
                selectedGapEnd = null
            }
        )
    }

    if (selectedTask != null) {
        TaskDetailSheet(
            task = selectedTask!!,
            onDismissRequest = viewModel::onDismissTaskDetails,
            onDelete = viewModel::onDeleteTask,
            onDeleteAll = viewModel::onDeleteAllOccurrences,
            onToggleComplete = { viewModel.onToggleCompletion(selectedTask!!) },
            onEdit = {
                taskToEdit = selectedTask
                isGroupEditMode = false
                viewModel.onDismissTaskDetails()
                showCreateTaskSheet = true
            },
            onEditAll = {
                taskToEdit = selectedTask
                isGroupEditMode = true
                viewModel.onDismissTaskDetails()
                showCreateTaskSheet = true
            },
        )
    }
}

// --- ESTADO VACÍO (Adaptado al Tema Oscuro) ---
@Composable
private fun EmptyStateMessage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EventBusy,
                contentDescription = null,
                // Gris suave para no distraer
                tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
        }

        Text(
            text = "Todo despejado",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface // Blanco
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "No tienes actividades programadas.\n¡Toca el + para empezar!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, // Gris medio
            textAlign = TextAlign.Center
        )
    }
}