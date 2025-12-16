package com.example.app_de_gestion_de_horarios_movil.ui.features.create_task

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.ui.components.ColorSelectorRow
import com.example.app_de_gestion_de_horarios_movil.ui.components.IconSelectorRow
import com.example.app_de_gestion_de_horarios_movil.ui.components.ReadOnlyRow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel



// Extension function para formato de hora (HH:mm)
fun LocalTime.toUiString(): String {
    val h = hour.toString().padStart(2, '0')
    val m = minute.toString().padStart(2, '0')
    return "$h:$m"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskSheet(
    taskToEdit: Task? = null,
    onDismiss: () -> Unit,
    viewModel: CreateTaskViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    // Inicialización: Solo se ejecuta la primera vez que se abre el Sheet
    LaunchedEffect(Unit) {
        viewModel.setTaskToEdit(taskToEdit)
    }

    LaunchedEffect(state.isTaskSaved) {
        if (state.isTaskSaved) {
            onDismiss()
            viewModel.resetState()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.imePadding()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp) // Un poco más de aire
        ) {
            Text("Nueva Tarea", style = MaterialTheme.typography.headlineSmall)

            // 1. TÍTULO
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("¿Qué vas a hacer?") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 2. FECHA Y HORA (Layout mejorado)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Fecha
                Box(modifier = Modifier.weight(1f)) {
                    ReadOnlyRow(
                        text = state.selectedDate.toString(), // Puedes formatear fecha aquí también si quieres
                        icon = Icons.Default.DateRange,
                        onClick = { showDatePicker = true }
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Hora Inicio
                Column(modifier = Modifier.weight(1f)) {
                    Text("Inicio", style = MaterialTheme.typography.labelSmall)
                    ReadOnlyRow(
                        // AQUÍ APLICAMOS LA CORRECCIÓN VISUAL
                        text = state.startTime.toUiString(),
                        icon = Icons.Default.Schedule,
                        onClick = { showStartTimePicker = true }
                    )
                }

                // Hora Fin
                Column(modifier = Modifier.weight(1f)) {
                    Text("Fin", style = MaterialTheme.typography.labelSmall)
                    ReadOnlyRow(
                        // AQUÍ APLICAMOS LA CORRECCIÓN VISUAL
                        text = state.endTime.toUiString(),
                        icon = Icons.Default.Schedule,
                        onClick = { showEndTimePicker = true }
                    )
                }
            }

            // 3. ICONO (Nuevo Bloque)
            Column {
                Text("Icono", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))

                IconSelectorRow(
                    selectedIconId = state.selectedIconId,
                    onIconSelected = viewModel::onIconSelected
                )
            }

            // 4. COLOR SELECTOR (Nuevo Componente Inline)
            Column {
                Text("Etiqueta de Color", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                ColorSelectorRow(
                    selectedColorHex = state.selectedColorHex,
                    onColorSelected = viewModel::onColorSelected
                )
            }

            // 5. DESCRIPCIÓN
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Notas adicionales") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            if (state.error != null) {
                Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = viewModel::saveTask,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !state.isLoading && state.title.isNotBlank()
            ) {
                if(state.isLoading)
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                else
                    Text("Guardar Tarea")
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

