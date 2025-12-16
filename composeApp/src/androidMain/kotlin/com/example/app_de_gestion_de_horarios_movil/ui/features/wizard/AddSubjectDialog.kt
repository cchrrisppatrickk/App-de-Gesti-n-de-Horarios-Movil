package com.example.app_de_gestion_de_horarios_movil.ui.features.wizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app_de_gestion_de_horarios_movil.ui.components.DayOfWeekSelector
import com.example.app_de_gestion_de_horarios_movil.ui.components.ReadOnlyRow
import com.example.app_de_gestion_de_horarios_movil.ui.features.create_task.TimePickerDialogWrapper
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

@Composable
fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, days: List<DayOfWeek>, start: LocalTime, end: LocalTime) -> Unit
) {
    // Estado local del diálogo
    var name by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(listOf<DayOfWeek>()) }
    var startTime by remember { mutableStateOf(LocalTime(8, 0)) }
    var endTime by remember { mutableStateOf(LocalTime(10, 0)) }

    // Control de TimePickers
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Materia") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // 1. Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre (Ej: Cálculo)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 2. Días de la semana
                Text("Días de clase:", style = MaterialTheme.typography.labelLarge)
                DayOfWeekSelector(
                    selectedDays = selectedDays,
                    onDaySelected = { day ->
                        // Lógica de toggle: Si existe lo quita, si no existe lo agrega
                        selectedDays = if (selectedDays.contains(day)) {
                            selectedDays - day
                        } else {
                            selectedDays + day
                        }
                    }
                )

                // 3. Horas
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Inicio", style = MaterialTheme.typography.labelSmall)
                        ReadOnlyRow(
                            text = startTime.toString(),
                            icon = Icons.Default.Schedule,
                            onClick = { showStartPicker = true }
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Fin", style = MaterialTheme.typography.labelSmall)
                        ReadOnlyRow(
                            text = endTime.toString(),
                            icon = Icons.Default.Schedule,
                            onClick = { showEndPicker = true }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && selectedDays.isNotEmpty()) {
                        onConfirm(name, selectedDays, startTime, endTime)
                        onDismiss()
                    }
                },
                // Desactivar si no hay nombre o días
                enabled = name.isNotBlank() && selectedDays.isNotEmpty()
            ) { Text("Agregar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

    // --- Time Pickers ---
    if (showStartPicker) {
        TimePickerDialogWrapper(
            onDismiss = { showStartPicker = false },
            initialTime = startTime,
            onTimeSelected = { startTime = it }
        )
    }
    if (showEndPicker) {
        TimePickerDialogWrapper(
            onDismiss = { showEndPicker = false },
            initialTime = endTime,
            onTimeSelected = { endTime = it }
        )
    }
}