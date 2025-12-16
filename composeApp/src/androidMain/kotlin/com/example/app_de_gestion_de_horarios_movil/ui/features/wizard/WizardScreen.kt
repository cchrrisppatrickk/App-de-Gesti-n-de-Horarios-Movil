package com.example.app_de_gestion_de_horarios_movil.ui.features.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_de_gestion_de_horarios_movil.domain.model.SubjectConfig
import com.example.app_de_gestion_de_horarios_movil.ui.components.ReadOnlyRow
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardScreen(
    onFinished: () -> Unit, // Callback para navegar al Home al terminar
    viewModel: WizardViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    // Si terminó de generar, navegamos fuera
    if (state.isFinished) {
        onFinished()
    }

    if (showAddDialog) {
        AddSubjectDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, days, start, end ->
                // Colores hardcodeados por ahora, luego haremos random
                viewModel.addSubject(name, "#9B59B6", days, start, end)
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Configurar Horario") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Materia")
            }
        },
        bottomBar = {
            // Botón de Acción Principal: GENERAR
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = viewModel::generateSchedule,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !state.isGenerating && state.subjects.isNotEmpty()
                ) {
                    if (state.isGenerating) {
                        Text("Generando Horario...")
                    } else {
                        Text("GENERAR ${state.subjects.size} MATERIAS")
                        Spacer(modifier = Modifier.size(8.dp))
                        Icon(Icons.Default.ArrowForward, null)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // SECCIÓN 1: FECHAS DEL CICLO
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Duración del Ciclo/Semestre", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Aquí irían DatePickers reales, usamos ReadOnly por brevedad
                        Box(Modifier.weight(1f)) {
                            ReadOnlyRow(state.startDate.toString(), Icons.Default.DateRange, {})
                        }
                        Box(Modifier.weight(1f)) {
                            ReadOnlyRow(state.endDate.toString(), Icons.Default.DateRange, {})
                        }
                    }
                }
            }

            Text("Materias Agregadas (${state.subjects.size})", style = MaterialTheme.typography.titleMedium)

            // SECCIÓN 2: LISTA DE MATERIAS
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.subjects) { subject ->
                    SubjectItem(subject, onDelete = { viewModel.removeSubject(subject.id) })
                }
            }
        }
    }
}

@Composable
fun SubjectItem(subject: SubjectConfig, onDelete: () -> Unit) {
    Card {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bolita de color
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(subject.colorHex)))
            )

            Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Text(subject.name, style = MaterialTheme.typography.titleMedium)
                // Mostrar resumen de horarios (Ej: "MONDAY, WEDNESDAY")
                val daysSummary = subject.schedules.joinToString(", ") {
                    it.dayOfWeek.name.take(3) // "MON", "WED"
                }
                Text(daysSummary, style = MaterialTheme.typography.bodySmall)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Borrar")
            }
        }
    }
}