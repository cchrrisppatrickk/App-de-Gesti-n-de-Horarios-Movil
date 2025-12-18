package com.example.app_de_gestion_de_horarios_movil.ui.features.create_task

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_de_gestion_de_horarios_movil.domain.model.RecurrenceMode
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.ui.components.ColorSelectorRow
import com.example.app_de_gestion_de_horarios_movil.ui.components.DayOfWeekSelector
import com.example.app_de_gestion_de_horarios_movil.ui.components.IconSelectorRow
import com.example.app_de_gestion_de_horarios_movil.ui.components.ReadOnlyRow
import kotlinx.datetime.Instant
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
    initialStartTime: LocalTime? = null,
    initialEndTime: LocalTime? = null,
    onDismiss: () -> Unit,
    viewModel: CreateTaskViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Estados para los diálogos
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showRecurrenceEndDatePicker by remember { mutableStateOf(false) }

    // Inicializaciones
    LaunchedEffect(Unit) {
        viewModel.setTaskToEdit(taskToEdit)
    }

    LaunchedEffect(state.isTaskSaved) {
        if (state.isTaskSaved) {
            onDismiss()
            viewModel.resetState()
        }
    }

    LaunchedEffect(initialStartTime, initialEndTime) {
        if (initialStartTime != null && initialEndTime != null) {
            viewModel.onStartTimeChange(initialStartTime)
            viewModel.onEndTimeChange(initialEndTime)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        // Agregamos navigationBarsPadding para que no choque con la barra de gestos del sistema
        modifier = Modifier
            .imePadding()
            .navigationBarsPadding()
    ) {
        // --- COLUMNA PRINCIPAL CON SCROLL ---
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                // IMPORTANTE: Habilitar scroll para ver el botón al final
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
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

            // 2. FECHA Y HORA
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Fecha
                Box(modifier = Modifier.weight(1f)) {
                    ReadOnlyRow(
                        text = state.selectedDate.toString(),
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
                        text = state.startTime.toUiString(),
                        icon = Icons.Default.Schedule,
                        onClick = { showStartTimePicker = true }
                    )
                }

                // Hora Fin
                Column(modifier = Modifier.weight(1f)) {
                    Text("Fin", style = MaterialTheme.typography.labelSmall)
                    ReadOnlyRow(
                        text = state.endTime.toUiString(),
                        icon = Icons.Default.Schedule,
                        onClick = { showEndTimePicker = true }
                    )
                }
            }

            // 3. ICONO Y COLOR
            Column {
                Text("Icono", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                IconSelectorRow(
                    selectedIconId = state.selectedIconId,
                    onIconSelected = viewModel::onIconSelected
                )
            }

            Column {
                Text("Etiqueta de Color", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                ColorSelectorRow(
                    selectedColorHex = state.selectedColorHex,
                    onColorSelected = viewModel::onColorSelected
                )
            }

            // 4. DESCRIPCIÓN
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

            Divider()

            // 5. SECCIÓN DE REPETICIÓN
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Repetición", style = MaterialTheme.typography.labelMedium)

                // Selector de Modo
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    RecurrenceMode.values().forEach { mode ->
                        val isSelected = state.recurrenceMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onRecurrenceModeChange(mode) },
                            label = {
                                Text(when(mode) {
                                    RecurrenceMode.ONCE -> "No repetir"
                                    RecurrenceMode.DAILY -> "Diario"
                                    RecurrenceMode.WEEKLY -> "Semanal"
                                    RecurrenceMode.CUSTOM -> "Personalizado"
                                })
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }

                // Opciones Extra si es recurrente
                if (state.recurrenceMode != RecurrenceMode.ONCE) {
                    // Fecha límite
                    Text("Repetir hasta:", style = MaterialTheme.typography.bodySmall)
                    ReadOnlyRow(
                        text = state.recurrenceEndDate.toString(),
                        icon = Icons.Default.EventRepeat,
                        onClick = { showRecurrenceEndDatePicker = true }
                    )

                    // Días específicos (Custom)
                    if (state.recurrenceMode == RecurrenceMode.CUSTOM) {
                        Text("Días activos:", style = MaterialTheme.typography.bodySmall)
                        DayOfWeekSelector(
                            selectedDays = state.selectedRecurrenceDays,
                            onDaySelected = { viewModel.onRecurrenceDayToggle(it) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. BOTÓN GUARDAR (Ahora sí visible gracias al scroll)
            Button(
                onClick = viewModel::saveTask,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !state.isLoading && state.title.isNotBlank()
            ) {
                if(state.isLoading)
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                else
                    Text(if (taskToEdit == null) "Crear Tarea" else "Guardar Cambios")
            }
        }
    }

    // --- DIÁLOGOS ---

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.UTC).date
                        viewModel.onDateChange(date)
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showStartTimePicker) {
        TimePickerDialogWrapper(
            onDismiss = { showStartTimePicker = false },
            initialTime = state.startTime,
            onTimeSelected = { viewModel.onStartTimeChange(it) }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialogWrapper(
            onDismiss = { showEndTimePicker = false },
            initialTime = state.endTime,
            onTimeSelected = { viewModel.onEndTimeChange(it) }
        )
    }

    if (showRecurrenceEndDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showRecurrenceEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.UTC).date
                        viewModel.onRecurrenceEndDateChange(date)
                    }
                    showRecurrenceEndDatePicker = false
                }) { Text("Aceptar") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

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
        text = { TimePicker(state = timeState) }
    )
}