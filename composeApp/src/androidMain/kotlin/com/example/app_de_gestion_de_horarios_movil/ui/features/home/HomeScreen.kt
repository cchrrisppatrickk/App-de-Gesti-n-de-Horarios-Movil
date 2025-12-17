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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_de_gestion_de_horarios_movil.ui.components.*
import com.example.app_de_gestion_de_horarios_movil.ui.features.create_task.CreateTaskSheet
import kotlinx.datetime.toJavaLocalDateTime
import org.koin.androidx.compose.koinViewModel
import java.time.temporal.ChronoUnit
import androidx.compose.runtime.getValue // <--- IMPORTANTE PARA EL "by"
import androidx.compose.runtime.setValue // <--- IMPORTANTE PARA EL "by"
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import kotlinx.datetime.DatePeriod
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

    var showCreateTaskSheet by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    // --- CONFIGURACIÓN DEL PAGER ---

    // 1. Total de días
    val totalDays = remember(viewModel.calendarStartDate, viewModel.calendarEndDate) {
        ChronoUnit.DAYS.between(
            viewModel.calendarStartDate.toJavaLocalDate(),
            viewModel.calendarEndDate.toJavaLocalDate()
        ).toInt() + 1
    }

    // 2. Página inicial
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

    // --- SOLUCIÓN AL ERROR VISUAL ---

    // Detectamos si el usuario está tocando/arrastrando el Pager con el dedo
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

    // Bandera para saber si el scroll lo causó el código (clic en calendario)
    var isProgrammaticScroll by remember { mutableStateOf(false) }

    // A. Sincronización: CALENDARIO (ViewModel) -> PAGER
    LaunchedEffect(uiState.selectedDate) {
        val targetPage = ChronoUnit.DAYS.between(
            viewModel.calendarStartDate.toJavaLocalDate(),
            uiState.selectedDate.toJavaLocalDate()
        ).toInt()

        if (pagerState.currentPage != targetPage && targetPage in 0 until totalDays) {
            // Activamos la bandera: "Este movimiento es mío, ignóralo"
            isProgrammaticScroll = true
            pagerState.animateScrollToPage(targetPage)
            isProgrammaticScroll = false // Desactivamos al terminar
        }
    }

    // B. Sincronización: PAGER (Swipe) -> VIEWMODEL
    // Usamos snapshotFlow para observar cambios, pero filtramos por la bandera
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            // SOLO actualizamos si:
            // 1. NO es un scroll programático (clic en calendario)
            // 2. O SI el usuario está arrastrando explícitamente (isDragged)
            if (!isProgrammaticScroll || isDragged) {
                val newDate = viewModel.calendarStartDate.plus(DatePeriod(days = page))
                if (newDate != uiState.selectedDate) {
                    viewModel.onDateSelected(newDate)
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateTaskSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
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
            StripCalendar(
                selectedDate = uiState.selectedDate,
                eventsColors = calendarColors,
                startDate = viewModel.calendarStartDate,
                endDate = viewModel.calendarEndDate,
                onDateSelected = { newDate -> viewModel.onDateSelected(newDate) },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.surfaceVariant)

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalAlignment = Alignment.Top,
                // Agregamos un pequeño espacio entre páginas para evitar solapamiento visual
                pageSpacing = 16.dp
            ) { pageIndex ->

                val pageDate = viewModel.calendarStartDate.plus(DatePeriod(days = pageIndex))

                if (pageDate == uiState.selectedDate) {
                    // Contenido Real
                    Box(modifier = Modifier.fillMaxSize()) {
                        when {
                            uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            uiState.tasks.isEmpty() -> EmptyStateMessage(modifier = Modifier.align(Alignment.Center))
                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 80.dp)
                                ) {
                                    itemsIndexed(uiState.tasks) { index, task ->
                                        TimelineTaskRow(
                                            task = task,
                                            isFirst = index == 0,
                                            isLast = index == uiState.tasks.lastIndex,
                                            // NUEVO: Conectamos el botón de check
                                            onToggleCompletion = { viewModel.onToggleCompletion(task) },
                                            modifier = Modifier
                                                .padding(horizontal = 16.dp)
                                                // Mantenemos el click en toda la fila para ver detalles
                                                .clickable { viewModel.onTaskSelected(task) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Placeholder durante Swipe
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

    // --- SHEET DE CREAR / EDITAR ---
    if (showCreateTaskSheet) {
        CreateTaskSheet(
            taskToEdit = taskToEdit,
            onDismiss = {
                showCreateTaskSheet = false
                taskToEdit = null
            }
        )
    }

    // --- SHEET DE DETALLES ---
    if (selectedTask != null) {
        TaskDetailSheet(
            task = selectedTask!!,
            onDismissRequest = viewModel::onDismissTaskDetails,
            onDelete = viewModel::onDeleteTask,

            // CORRECCIÓN AQUÍ:
            // Antes: onToggleComplete = viewModel::onToggleCompletion
            // Ahora: Pasamos la tarea seleccionada explícitamente
            onToggleComplete = { viewModel.onToggleCompletion(selectedTask!!) },      onEdit = {
                taskToEdit = selectedTask
                viewModel.onDismissTaskDetails()
                showCreateTaskSheet = true
            }
        )
    }
}

// --- COMPONENTE PRIVADO: Mensaje de Lista Vacía ---
@Composable
private fun EmptyStateMessage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.EventBusy, // O un icono de "Playa/Relax"
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Todo despejado",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No tienes actividades programadas para este día.\n¡Toca el + para empezar!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}