package com.example.app_de_gestion_de_horarios_movil.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_de_gestion_de_horarios_movil.ui.components.StripCalendar
import com.example.app_de_gestion_de_horarios_movil.ui.components.TimelineTaskRow
import com.example.app_de_gestion_de_horarios_movil.ui.features.create_task.CreateTaskSheet
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onNavigateToWizard: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateTaskSheet by remember { mutableStateOf(false) }

    if (showCreateTaskSheet) {
        CreateTaskSheet(onDismiss = { showCreateTaskSheet = false })
    }

    Scaffold(
        // Usamos el contenedor para el fondo gris claro típico de apps de productividad
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),

        topBar = {
            // COLUMNA PERSONALIZADA: Barra de Acciones + Calendario
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {

                // 1. Mini Header con el botón del Wizard (estilo minimalista)
                TopAppBar(
                    title = {
                        Text(
                            text = "Tareas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                )

                // 2. NUESTRO STRIP CALENDAR
                StripCalendar(
                    selectedDate = state.selectedDate,
                    onDateSelected = { newDate ->
                        viewModel.onDateSelected(newDate)
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateTaskSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear Tarea")
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.tasks.isEmpty()) {
                // Estado vacío mejorado
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "☕",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        text = "Día libre",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "No tienes tareas programadas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // LA CRONOLOGÍA
                LazyColumn(
                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp, start = 16.dp, end = 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(state.tasks) { index, task ->
                        TimelineTaskRow(
                            task = task,
                            isFirst = index == 0,
                            isLast = index == state.tasks.lastIndex
                        )
                    }
                }
            }
        }
    }
}