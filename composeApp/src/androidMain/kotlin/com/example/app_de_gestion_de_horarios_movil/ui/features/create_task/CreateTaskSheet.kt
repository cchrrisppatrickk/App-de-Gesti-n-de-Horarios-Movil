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
import androidx.compose.material3.*
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

// Extension function para formato de hora visual
fun LocalTime.toUiString(): String {
    val amPm = if (hour >= 12) "PM" else "AM"
    val hour12 = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val m = minute.toString().padStart(2, '0')
    return "$hour12:$m $amPm"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskSheet(
    taskToEdit: Task? = null,
    isGroupEdit: Boolean = false,
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

    // Inicialización de datos
    LaunchedEffect(Unit) {
        viewModel.setTaskToEdit(taskToEdit, isGroupEdit = isGroupEdit)
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
        // CORRECCIÓN VISUAL: Fondo Surface (#252525) para contraste con fondo app (#161616)
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .imePadding()
            .navigationBarsPadding()
    ) {
        // --- COLUMNA PRINCIPAL CON SCROLL ---
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Nueva Tarea",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            // 1. TÍTULO
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("¿Qué vas a hacer?") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                // CORRECCIÓN VISUAL: Colores Coral para foco
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
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
                    Text("Inicio", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    ReadOnlyRow(
                        text = state.startTime.toUiString(),
                        icon = Icons.Default.Schedule,
                        onClick = { showStartTimePicker = true }
                    )
                }

                // Hora Fin
                Column(modifier = Modifier.weight(1f)) {
                    Text("Fin", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    ReadOnlyRow(
                        text = state.endTime.toUiString(),
                        icon = Icons.Default.Schedule,
                        onClick = { showEndTimePicker = true }
                    )
                }
            }

            // 3. ICONO Y COLOR
            Column {
                Text("Icono", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                IconSelectorRow(
                    selectedIconId = state.selectedIconId,
                    onIconSelected = viewModel::onIconSelected
                )
            }

            Column {
                Text("Etiqueta de Color", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
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
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            if (state.error != null) {
                Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // 5. SECCIÓN DE REPETICIÓN (CORREGIDA)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Repetición", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)

                // Selector de Modo
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    RecurrenceMode.values().forEach { mode ->
                        val isSelected = state.recurrenceMode == mode

                        FilterChip(
                            selected = isSelected, // CORRECCIÓN: Parámetro explícito
                            enabled = true,        // CORRECCIÓN: Parámetro explícito
                            onClick = { viewModel.onRecurrenceModeChange(mode) }, // CORRECCIÓN: Usar 'mode', no 'it'
                            label = {
                                Text(when(mode) {
                                    RecurrenceMode.ONCE -> "No repetir"
                                    RecurrenceMode.DAILY -> "Diario"
                                    RecurrenceMode.WEEKLY -> "Semanal"
                                    RecurrenceMode.CUSTOM -> "Personalizado"
                                })
                            },
                            modifier = Modifier.padding(end = 8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.outlineVariant,
                                selectedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                // Opciones Extra si es recurrente
                if (state.recurrenceMode != RecurrenceMode.ONCE) {
                    Text("Repetir hasta:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ReadOnlyRow(
                        text = state.recurrenceEndDate.toString(),
                        icon = Icons.Default.EventRepeat,
                        onClick = { showRecurrenceEndDatePicker = true }
                    )

                    // Días específicos (Custom)
                    if (state.recurrenceMode == RecurrenceMode.CUSTOM) {
                        Text("Días activos:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        DayOfWeekSelector(
                            selectedDays = state.selectedRecurrenceDays,
                            onDaySelected = { day -> viewModel.onRecurrenceDayToggle(day) } // CORRECCIÓN: Usar 'day'
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. BOTÓN GUARDAR
            Button(
                onClick = viewModel::saveTask,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !state.isLoading && state.title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary, // Coral
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
            // CORRECCIÓN: Usar 'time ->' explícitamente
            onTimeSelected = { time -> viewModel.onStartTimeChange(time) }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialogWrapper(
            onDismiss = { showEndTimePicker = false },
            initialTime = state.endTime,
            // CORRECCIÓN: Usar 'time ->' explícitamente
            onTimeSelected = { time -> viewModel.onEndTimeChange(time) }
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
        initialMinute = initialTime.minute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        // Forzamos el fondo al gris de tu tema (SurfaceDark)
        containerColor = MaterialTheme.colorScheme.surface,

        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(LocalTime(timeState.hour, timeState.minute))
                    onDismiss()
                },
                // El botón "Aceptar" en color Coral
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) { Text("Aceptar") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                // El botón "Cancelar" también en Coral (o gris si prefieres)
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            ) { Text("Cancelar") }
        },
        text = {
            TimePicker(
                state = timeState,
                // AQUÍ ESTÁ LA MAGIA: Personalizamos cada parte del reloj
                colors = TimePickerDefaults.colors(
                    // 1. Manecilla y selector (El círculo que mueves)
                    selectorColor = MaterialTheme.colorScheme.primary, // Coral

                    // 2. Fondo de los números seleccionados (La caja de la hora digital arriba)
                    timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    timeSelectorSelectedContentColor = MaterialTheme.colorScheme.primary,

                    // 3. Fondo de los números NO seleccionados
                    timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface,

                    // 4. El círculo grande del reloj (Fondo)
                    clockDialColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    clockDialSelectedContentColor = Color.White, // Color del número seleccionado en la esfera
                    clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface, // Color de números normales

                    // 5. Selector AM/PM
                    periodSelectorBorderColor = MaterialTheme.colorScheme.primary,
                    periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    periodSelectorUnselectedContainerColor = Color.Transparent,
                    periodSelectorSelectedContentColor = Color.White,
                    periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    )
}