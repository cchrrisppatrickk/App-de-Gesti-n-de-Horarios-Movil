package com.example.app_de_gestion_de_horarios_movil.ui.features.create_task

import android.os.Build
import androidx.annotation.RequiresApi
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
import com.example.app_de_gestion_de_horarios_movil.ui.components.*
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toKotlinLocalDate
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventSheet(
    onDismiss: () -> Unit,
    viewModel: CreateTaskViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // --- ESTADOS LOCALES PARA DIÁLOGOS ---
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    // --- FORMATTERS ---
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.getDefault()) }
    // Formato corto sin segundos: HH:mm (ej. 14:30)
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    LaunchedEffect(Unit) {
        viewModel.setEntryType(TaskType.EVENT)
    }

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
                .padding(bottom = 32.dp)
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

            // TÍTULO
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

            // FECHA
            ReadOnlyRow(
                // Usamos el formatter para mostrar la fecha bonita
                text = state.selectedDate.toJavaLocalDate().format(dateFormatter).replaceFirstChar { it.uppercase() },
                icon = Icons.Default.DateRange,
                onClick = { showDatePicker = true } // ABRE DIÁLOGO
            )

            // HORAS
            if (!state.isAllDay) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        ReadOnlyRow(
                            // Formato limpio HH:mm
                            text = state.startTime.toJavaLocalTime().format(timeFormatter),
                            icon = Icons.Default.Schedule,
                            onClick = { showStartTimePicker = true } // ABRE DIÁLOGO
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        ReadOnlyRow(
                            // Formato limpio HH:mm
                            text = state.endTime.toJavaLocalTime().format(timeFormatter),
                            icon = Icons.Default.Schedule,
                            onClick = { showEndTimePicker = true } // ABRE DIÁLOGO
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // COLOR
            ColorSelectorRow(
                selectedColorHex = state.selectedColorHex,
                onColorSelected = viewModel::onColorSelected
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // --- DIÁLOGOS EMERGENTES ---

    // 1. DATE PICKER
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.selectedDate.toJavaLocalDate()
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                            .toKotlinLocalDate()
                        viewModel.onDateChange(selectedDate)
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // 2. TIME PICKER (INICIO)
    if (showStartTimePicker) {
        TimePickerDialogWrapper(
            onDismiss = { showStartTimePicker = false },
            initialTime = state.startTime,
            onTimeSelected = { newTime ->
                viewModel.onStartTimeChange(newTime)
            }
        )
    }

    // 3. TIME PICKER (FIN)
    if (showEndTimePicker) {
        TimePickerDialogWrapper(
            onDismiss = { showEndTimePicker = false },
            initialTime = state.endTime,
            onTimeSelected = { newTime ->
                viewModel.onEndTimeChange(newTime)
            }
        )
    }
}