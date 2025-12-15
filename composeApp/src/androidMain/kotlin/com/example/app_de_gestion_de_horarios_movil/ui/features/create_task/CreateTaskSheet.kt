package com.example.app_de_gestion_de_horarios.ui.features.create_task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_de_gestion_de_horarios.ui.components.ReadOnlyRow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskSheet(
    onDismiss: () -> Unit,
    viewModel: CreateTaskViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Estados para controlar qué diálogo se muestra
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    // Efecto: Si se guardó correctamente, cerramos el modal
    LaunchedEffect(state.isTaskSaved) {
        if (state.isTaskSaved) {
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        // imePadding ayuda a que el teclado no tape el contenido
        modifier = Modifier.imePadding()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp), // Espacio abajo
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Nueva Tarea",
                style = MaterialTheme.typography.headlineSmall
            )

            // 1. TÍTULO
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 2. SELECTOR DE FECHA
            ReadOnlyRow(
                text = "Fecha: ${state.selectedDate}",
                icon = Icons.Default.DateRange,
                onClick = { showDatePicker = true }
            )

            // 3. SELECTORES DE HORA (Inicio y Fin en una fila)
            Row(modifier = Modifier.fillMaxWidth()) {
                // Inicio
                Column(modifier = Modifier.weight(1f)) {
                    Text("Inicio", style = MaterialTheme.typography.labelMedium)
                    ReadOnlyRow(
                        text = state.startTime.toString(),
                        icon = Icons.Default.Schedule,
                        onClick = { showStartTimePicker = true }
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                // Fin
                Column(modifier = Modifier.weight(1f)) {
                    Text("Fin", style = MaterialTheme.typography.labelMedium)
                    ReadOnlyRow(
                        text = state.endTime.toString(),
                        icon = Icons.Default.Schedule,
                        onClick = { showEndTimePicker = true }
                    )
                }
            }

            // 4. DESCRIPCIÓN (Opcional)
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Notas (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // Error Message
            if (state.error != null) {
                Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
            }

            // 5. BOTÓN GUARDAR
            Button(
                onClick = viewModel::saveTask,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                Text(if (state.isLoading) "Guardando..." else "Crear Tarea")
            }
        }
    }

    // --- DIÁLOGOS (Popups) ---

    // A. DIÁLOGO DE FECHA
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Conversión de Millis (M3) a LocalDate (KotlinX)
                        val date = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.UTC).date
                        viewModel.onDateChange(date)
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // B. DIÁLOGO HORA INICIO
    if (showStartTimePicker) {
        TimePickerDialogWrapper(
            onDismiss = { showStartTimePicker = false },
            initialTime = state.startTime,
            onTimeSelected = { viewModel.onStartTimeChange(it) }
        )
    }

    // C. DIÁLOGO HORA FIN
    if (showEndTimePicker) {
        TimePickerDialogWrapper(
            onDismiss = { showEndTimePicker = false },
            initialTime = state.endTime,
            onTimeSelected = { viewModel.onEndTimeChange(it) }
        )
    }
}

// Wrapper simple para el TimePicker (ya que M3 no tiene un TimePickerDialog oficial estable aún)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialogWrapper(
    onDismiss: () -> Unit,
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit
) {
    val timeState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute
    )

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onTimeSelected(LocalTime(timeState.hour, timeState.minute))
                onDismiss()
            }) { Text("Aceptar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        text = {
            TimePicker(state = timeState)
        }
    )
}