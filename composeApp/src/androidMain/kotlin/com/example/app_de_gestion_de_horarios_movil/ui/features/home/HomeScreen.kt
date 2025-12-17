package com.example.app_de_gestion_de_horarios_movil.ui.features.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
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

    // 1. CONFIGURACIÓN DEL PAGER (SWIPE DE DÍAS)
    // Calculamos el total de días soportados por el calendario
    val totalDays = remember(viewModel.calendarStartDate, viewModel.calendarEndDate) {
        ChronoUnit.DAYS.between(
            viewModel.calendarStartDate.toJavaLocalDate(),
            viewModel.calendarEndDate.toJavaLocalDate()
        ).toInt() + 1
    }

    // Calculamos la página inicial basada en la fecha seleccionada actual
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

    // 2. SINCRONIZACIÓN: CALENDARIO CLICK -> MOVER PAGER
    // Si la fecha cambia desde el ViewModel (ej: click en StripCalendar), movemos el Pager
    LaunchedEffect(uiState.selectedDate) {
        val targetPage = ChronoUnit.DAYS.between(
            viewModel.calendarStartDate.toJavaLocalDate(),
            uiState.selectedDate.toJavaLocalDate()
        ).toInt()

        if (pagerState.currentPage != targetPage && targetPage in 0 until totalDays) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    // 3. SINCRONIZACIÓN: SWIPE PAGER -> CAMBIAR FECHA VIEWMODEL
    // Si el usuario desliza el Pager, actualizamos la fecha en el ViewModel
    LaunchedEffect(pagerState.currentPage) {
        val newDate = viewModel.calendarStartDate.  plus(DatePeriod(days = pagerState.currentPage))
        if (newDate != uiState.selectedDate) {
            viewModel.onDateSelected(newDate)
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
            // A. CALENDARIO SUPERIOR
            StripCalendar(
                selectedDate = uiState.selectedDate,
                eventsColors = calendarColors,
                startDate = viewModel.calendarStartDate,
                endDate = viewModel.calendarEndDate,
                onDateSelected = { newDate -> viewModel.onDateSelected(newDate) },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.surfaceVariant)

            // B. CUERPO DESLIZABLE (HorizontalPager)
            // Esto reemplaza al Box estático anterior
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                // Alineación vertical para que el contenido no flote raro
                verticalAlignment = Alignment.Top
            ) { pageIndex ->

                // Calculamos qué fecha corresponde a ESTA página del swipe
                val pageDate = viewModel.calendarStartDate.plus(DatePeriod(days = pageIndex))

                // TRUCO DE RENDIMIENTO Y UX:
                // El ViewModel solo tiene la lista de tareas de la "selectedDate".
                // Si el Pager está renderizando la página de mañana (pageIndex + 1) para la animación,
                // NO debemos mostrar la lista de hoy, o se verá duplicada.

                if (pageDate == uiState.selectedDate) {
                    // --- MOSTRAMOS EL CONTENIDO REAL (porque es el día seleccionado) ---
                    Box(modifier = Modifier.fillMaxSize()) {
                        when {
                            uiState.isLoading -> {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                            uiState.tasks.isEmpty() -> {
                                EmptyStateMessage(modifier = Modifier.align(Alignment.Center))
                            }
                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 80.dp)
                                ) {
                                    itemsIndexed(uiState.tasks) { index, task ->
                                        val isFirst = index == 0
                                        val isLast = index == uiState.tasks.lastIndex

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { viewModel.onTaskSelected(task) }
                                        ) {
                                            TimelineTaskRow(
                                                task = task,
                                                isFirst = isFirst,
                                                isLast = isLast,
                                                modifier = Modifier.padding(horizontal = 16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // --- MOSTRAMOS UN PLACEHOLDER ---
                    // Mientras deslizas, la página vecina muestra un estado de carga limpio
                    // hasta que sueltas el dedo y el ViewModel carga los datos reales.
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(30.dp),
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
            onToggleComplete = viewModel::onToggleCompletion,
            onEdit = {
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