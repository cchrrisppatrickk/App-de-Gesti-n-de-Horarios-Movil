package com.example.app_de_gestion_de_horarios.ui.features.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_de_gestion_de_horarios.ui.components.TimelineTaskRow
import org.koin.androidx.compose.koinViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class) // Para TopAppBar
@Composable
fun HomeScreen(
    // Koin inyecta el ViewModel automáticamente aquí
    viewModel: HomeViewModel = koinViewModel()
) {
    // Consumimos el estado de forma segura para el ciclo de vida
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            // Por ahora un TopBar simple, luego haremos el Calendario Strip
            TopAppBar(title = { Text("Cronología: ${state.selectedDate}") })
        }
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.tasks.isEmpty()) {
                // Estado vacío
                Text(
                    text = "No hay tareas para hoy",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // LA CRONOLOGÍA
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    // Usamos itemsIndexed para saber cuál es el primero y el último
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