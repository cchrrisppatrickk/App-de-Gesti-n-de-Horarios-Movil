package com.example.app_de_gestion_de_horarios_movil.ui.features.create_task

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app_de_gestion_de_horarios_movil.domain.model.NotificationType
import com.example.app_de_gestion_de_horarios_movil.domain.model.RecurrenceMode
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.ui.components.ColorSelectorRow
import com.example.app_de_gestion_de_horarios_movil.ui.components.DayOfWeekSelector
import com.example.app_de_gestion_de_horarios_movil.ui.components.IconSelectorRow
import com.example.app_de_gestion_de_horarios_movil.ui.components.TimePickerDialogWrapper
import com.example.app_de_gestion_de_horarios_movil.ui.components.toUiString
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel


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

    // Estados de diálogos
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showRecurrenceEndDatePicker by remember { mutableStateOf(false) }

    // Inicializaciones
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
        // CAMBIO VISUAL: Fondo "Background" (Oscuro) para que las tarjetas "Surface" (Gris) resalten
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.imePadding().navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp) // Más espacio entre secciones
        ) {
            // Cabecera simple
            Text(
                text = if (taskToEdit == null) "Nueva Tarea" else "Editar Tarea",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // 1. TÍTULO (Input Principal)
            // Lo mantenemos fuera de tarjeta para darle máxima jerarquía, pero con estilo limpio
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Título de la tarea") },
                placeholder = { Text("Ej: Clase de Matemáticas") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                leadingIcon = {
                    Icon(Icons.Default.Title, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            )

            // 2. HORARIO (Agrupado en Tarjeta)
            FormSection(title = "Horario") {
                // Fecha
                ClickableFormRow(
                    icon = Icons.Default.DateRange,
                    label = "Fecha",
                    value = state.selectedDate.toString(),
                    onClick = { showDatePicker = true }
                )
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                // Hora Inicio
                ClickableFormRow(
                    icon = Icons.Default.Schedule,
                    label = "Hora de inicio",
                    value = state.startTime.toUiString(),
                    onClick = { showStartTimePicker = true }
                )
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                // Hora Fin
                ClickableFormRow(
                    icon = Icons.Default.Schedule,
                    label = "Hora de fin",
                    value = state.endTime.toUiString(),
                    onClick = { showEndTimePicker = true }
                )
            }

            // 3. APARIENCIA (Agrupado en Tarjeta)
            FormSection(title = "Apariencia") {
                Column(modifier = Modifier.padding(vertical = 12.dp)) {
                    Text(
                        "Icono",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )
                    IconSelectorRow(
                        selectedIconId = state.selectedIconId,
                        onIconSelected = viewModel::onIconSelected
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Color",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )
                    ColorSelectorRow(
                        selectedColorHex = state.selectedColorHex,
                        onColorSelected = viewModel::onColorSelected
                    )
                }
            }

            // 4. RECORDATORIOS (Checkbox en Tarjeta)
            FormSection(title = "Notificaciones") {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    AlertCheckboxRow(
                        label = "Al inicio del evento",
                        checked = state.selectedAlerts.contains(NotificationType.AT_START),
                        onCheckedChange = { viewModel.onAlertToggle(NotificationType.AT_START) }
                    )
                    AlertCheckboxRow(
                        label = "15 minutos antes",
                        checked = state.selectedAlerts.contains(NotificationType.FIFTEEN_MIN_BEFORE),
                        onCheckedChange = { viewModel.onAlertToggle(NotificationType.FIFTEEN_MIN_BEFORE) }
                    )
                    AlertCheckboxRow(
                        label = "Al finalizar",
                        checked = state.selectedAlerts.contains(NotificationType.AT_END),
                        onCheckedChange = { viewModel.onAlertToggle(NotificationType.AT_END) }
                    )
                }
            }

            // 5. NOTAS ADICIONALES
            FormSection(title = "Notas") {
                TextField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    placeholder = { Text("Detalles, materiales, ubicación...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                )
            }

            // 6. REPETICIÓN
            FormSection(title = "Repetición") {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                modifier = Modifier.padding(end = 8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
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

                    if (state.recurrenceMode != RecurrenceMode.ONCE) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Termina el:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Usamos una variante pequeña de ClickableRow aquí
                        Surface(
                            onClick = { showRecurrenceEndDatePicker = true },
                            color = MaterialTheme.colorScheme.background, // Contraste interno
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.EventRepeat, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(state.recurrenceEndDate.toString(), style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        if (state.recurrenceMode == RecurrenceMode.CUSTOM) {
                            Spacer(modifier = Modifier.height(16.dp))
                            DayOfWeekSelector(
                                selectedDays = state.selectedRecurrenceDays,
                                onDaySelected = { day -> viewModel.onRecurrenceDayToggle(day) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 7. BOTÓN GUARDAR
            Button(
                onClick = viewModel::saveTask,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.isLoading && state.title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if(state.isLoading)
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                else
                    Text(
                        if (taskToEdit == null) "Crear Tarea" else "Guardar Cambios",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
            }
        }
    }

    // --- DIALOGOS (Sin cambios en lógica) ---
    // (Mantén los diálogos DatePicker, TimePicker, etc. exactamente igual que antes)
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
                        viewModel.onDateChange(date)
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showStartTimePicker) {
        TimePickerDialogWrapper(onDismiss = { showStartTimePicker = false }, initialTime = state.startTime, onTimeSelected = viewModel::onStartTimeChange)
    }
    if (showEndTimePicker) {
        TimePickerDialogWrapper(onDismiss = { showEndTimePicker = false }, initialTime = state.endTime, onTimeSelected = viewModel::onEndTimeChange)
    }
    if (showRecurrenceEndDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showRecurrenceEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
                        viewModel.onRecurrenceEndDateChange(date)
                    }
                    showRecurrenceEndDatePicker = false
                }) { Text("Aceptar") }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

// ----------------------------------------------------------------
// COMPONENTES VISUALES (Helpers para el nuevo diseño)
// ----------------------------------------------------------------

@Composable
fun FormSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface // Tarjeta Gris
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            content()
        }
    }
}

@Composable
fun ClickableFormRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono con fondo suave (Squircle)
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        // Valor
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun AlertCheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.outline
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

