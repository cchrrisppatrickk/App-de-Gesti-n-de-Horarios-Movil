package com.example.app_de_gestion_de_horarios_movil.ui.features.create_task

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.app_de_gestion_de_horarios_movil.domain.model.RecurrenceMode
import com.example.app_de_gestion_de_horarios_movil.ui.components.* // Tus componentes reutilizables
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventSheet(
    onDismiss: () -> Unit,
    viewModel: CreateTaskViewModel = koinViewModel() // Reutilizamos el mismo VM
) {
    val state by viewModel.uiState.collectAsState()

    // 1. Inicializar como EVENTO al abrir
    LaunchedEffect(Unit) {
        viewModel.setEntryType(TaskType.EVENT)
    }

    // 2. Cerrar si se guardó con éxito
    LaunchedEffect(state.isTaskSaved) {
        if (state.isTaskSaved) {
            onDismiss()
            viewModel.resetState()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp) // Espacio para el teclado/navegación
        ) {
            // CABECERA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cancelar")
                }
                Text(
                    text = "Nuevo Evento",
                    style = MaterialTheme.typography.titleLarge
                )
                Button(
                    onClick = { viewModel.saveTask() },
                    enabled = state.title.isNotBlank() && !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    } else {
                        Text("Guardar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TÍTULO (Grande y claro)
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Añadir título") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // SWITCH "TODO EL DÍA"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Todo el día")
                Switch(
                    checked = state.isAllDay,
                    onCheckedChange = viewModel::onAllDayToggle
                )
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // FECHA Y HORA (Reutilizando tus componentes)
            // Si es "Todo el día", ocultamos las horas
            ReadOnlyRow(
                text = state.selectedDate.toString(), // TODO: Formatear fecha bonita
                icon = Icons.Default.DateRange,
                onClick = { /* Abrir DatePicker */ } // Integra tu lógica de DatePicker aquí
            )

            if (!state.isAllDay) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        ReadOnlyRow(
                            text = state.startTime.toString(), // TODO: Formatear
                            icon = Icons.Default.Schedule,
                            onClick = { /* Abrir TimePicker Inicio */ }
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        ReadOnlyRow(
                            text = state.endTime.toString(), // TODO: Formatear
                            icon = Icons.Default.Schedule,
                            onClick = { /* Abrir TimePicker Fin */ }
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // COLOR (Reutilizando tu selector)
            ColorSelectorRow(
                selectedColorHex = state.selectedColorHex,
                onColorSelected = viewModel::onColorSelected
            )

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // REPETICIÓN Y ALERTAS (Simplificado o completo según gustes)
            // Aquí puedes reutilizar los mismos componentes que CreateTaskSheet
            // para RecurrenceMode y Notifications.

            Text("Más opciones...", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        }
    }
}