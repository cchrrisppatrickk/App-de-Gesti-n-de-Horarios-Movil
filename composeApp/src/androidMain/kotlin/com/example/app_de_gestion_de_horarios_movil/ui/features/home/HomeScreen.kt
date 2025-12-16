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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    // Inyección automática del ViewModel con Koin
    viewModel: HomeViewModel = koinViewModel(),
    // Callback para navegar al Wizard (si tienes un botón para ello en la UI)
    onNavigateToWizard: () -> Unit
) {
    // 1. Colección de Estados desde el ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTask by viewModel.selectedTask.collectAsStateWithLifecycle()

    // Estado local para mostrar/ocultar el formulario de "Crear Tarea" (FAB)
    var showCreateTaskSheet by remember { mutableStateOf(false) }

    // 2. Estructura Principal con Scaffold
    Scaffold(
        // Botón Flotante (+)
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
                // Importante: paddingValues evita que el contenido quede bajo la BottomBar
                .padding(paddingValues)
        ) {
            // A. CALENDARIO SUPERIOR (Strip Calendar)
            // Se mantiene fijo en la parte superior
            StripCalendar(
                selectedDate = uiState.selectedDate,
                onDateSelected = { newDate -> viewModel.onDateSelected(newDate) },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.surfaceVariant)

            // B. CUERPO DE LA CRONOLOGÍA
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f) // Ocupa el resto del espacio
            ) {
                when {
                    // Caso 1: Cargando
                    uiState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    // Caso 2: Lista Vacía (Día libre)
                    uiState.tasks.isEmpty() -> {
                        EmptyStateMessage(modifier = Modifier.align(Alignment.Center))
                    }

                    // Caso 3: Lista de Tareas (Timeline)
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            // Añadimos padding al final para que el FAB no tape la última tarea
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            itemsIndexed(uiState.tasks) { index, task ->

                                // Lógica visual para la línea de tiempo (conectar puntos)
                                val isFirst = index == 0
                                val isLast = index == uiState.tasks.lastIndex

                                // C. RENDERIZADO DE LA TAREA
                                // Envolvemos en Box clickable para abrir detalles
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.onTaskSelected(task) } // <--- ABRE EL SHEET
                                ) {
                                    TimelineTaskRow(
                                        task = task,
                                        isFirst = isFirst,
                                        isLast = isLast,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }

                                // D. LÓGICA DE HUECOS (GAPS) - SUGERENCIAS
                                // Si no es la última tarea, miramos la siguiente
                                if (!isLast) {
                                    val nextTask = uiState.tasks[index + 1]

                                    // Calculamos la diferencia en minutos entre Fin Actual e Inicio Siguiente
                                    val currentEnd = task.endTime.toJavaLocalDateTime()
                                    val nextStart = nextTask.startTime.toJavaLocalDateTime()

                                    val minutesGap = ChronoUnit.MINUTES.between(currentEnd, nextStart)

//                                    // Si hay un hueco decente (>= 15 min), mostramos la sugerencia
//                                    if (minutesGap >= 15) {
//                                        FreeTimeGap(minutesFree = minutesGap)
//                                    }
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

    // 1. Modal de CREAR Tarea (Disparado por el FAB)
    if (showCreateTaskSheet) {
        CreateTaskSheet(
            onDismiss = { showCreateTaskSheet = false }
            // Nota: viewModel se inyecta internamente en CreateTaskSheet con koinViewModel()
        )
    }

    // 2. Modal de DETALLES (Disparado al tocar una tarea)
    if (selectedTask != null) {
        TaskDetailSheet(
            task = selectedTask!!,
            onDismissRequest = viewModel::onDismissTaskDetails,
            onDelete = viewModel::onDeleteTask,
            onEdit = viewModel::onEditTask,
            onToggleComplete = viewModel::onToggleCompletion
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