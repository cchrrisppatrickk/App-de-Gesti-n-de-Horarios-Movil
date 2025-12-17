package com.example.app_de_gestion_de_horarios_movil.ui.features.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onNavigateToWizard: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTask by viewModel.selectedTask.collectAsStateWithLifecycle()
    // Obtenemos los colores para los puntitos del calendario
    val calendarColors by viewModel.calendarColors.collectAsStateWithLifecycle()

    var showCreateTaskSheet by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

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
                .padding(paddingValues) // Respetamos el padding del Scaffold
        ) {
            // A. CALENDARIO SUPERIOR (Strip Calendar)
            // CORREGIDO: Ahora pasamos todos los parámetros requeridos
            StripCalendar(
                selectedDate = uiState.selectedDate,
                eventsColors = calendarColors,        // <--- Faltaba esto
                startDate = viewModel.calendarStartDate, // <--- Faltaba esto
                endDate = viewModel.calendarEndDate,     // <--- Faltaba esto
                onDateSelected = { newDate -> viewModel.onDateSelected(newDate) },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.surfaceVariant)

            // B. CUERPO DE LA CRONOLOGÍA
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
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
        }
    }

    // --- ZONA DE MODALES (SHEETS) ---
    // Se definen al final para superponerse a todo

    // 1. SHEET DE CREAR / EDITAR
    if (showCreateTaskSheet) {
        CreateTaskSheet(
            taskToEdit = taskToEdit, // <--- Pasamos la tarea (o null)
            onDismiss = {
                showCreateTaskSheet = false
                taskToEdit = null // Limpiamos al cerrar
            }
        )
    }

    // 2. SHEET DE DETALLES
    if (selectedTask != null) {
        TaskDetailSheet(
            task = selectedTask!!,
            onDismissRequest = viewModel::onDismissTaskDetails,
            onDelete = viewModel::onDeleteTask,
            onToggleComplete = viewModel::onToggleCompletion,

            // --- LÓGICA DE EDITAR ---
            onEdit = {
                taskToEdit = selectedTask // 1. Capturamos la tarea
                viewModel.onDismissTaskDetails() // 2. Cerramos detalles
                showCreateTaskSheet = true // 3. Abrimos formulario
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