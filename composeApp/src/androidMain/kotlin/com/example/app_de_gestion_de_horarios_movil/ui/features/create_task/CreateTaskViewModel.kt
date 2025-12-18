package com.example.app_de_gestion_de_horarios_movil.ui.features.create_task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_de_gestion_de_horarios_movil.domain.model.RecurrenceMode
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.domain.usecase.CreateRecurringTaskUseCase
import com.example.app_de_gestion_de_horarios_movil.domain.usecase.CreateTaskUseCase
import com.example.app_de_gestion_de_horarios_movil.domain.usecase.UpdateTaskGroupUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import java.util.UUID

class CreateTaskViewModel(
    private val createTaskUseCase: CreateTaskUseCase,
    private val createRecurringTaskUseCase: CreateRecurringTaskUseCase,
    private val updateTaskGroupUseCase: UpdateTaskGroupUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTaskUiState())
    val uiState = _uiState.asStateFlow()

    // Variable interna para saber si estamos en modo "Edición Masiva"
    private var editingGroupId: String? = null

    // --- EVENTOS DE ENTRADA (Inputs del usuario) ---

    fun onTitleChange(newTitle: String) = _uiState.update { it.copy(title = newTitle) }
    fun onDescriptionChange(newDesc: String) = _uiState.update { it.copy(description = newDesc) }
    fun onDateChange(newDate: LocalDate) = _uiState.update { it.copy(selectedDate = newDate) }
    fun onStartTimeChange(newTime: LocalTime) = _uiState.update { it.copy(startTime = newTime) }
    fun onEndTimeChange(newTime: LocalTime) = _uiState.update { it.copy(endTime = newTime) }

    // --- NUEVOS INPUTS ---
    fun onColorSelected(newColorHex: String) {
        _uiState.update { it.copy(selectedColorHex = newColorHex) }
    }

    fun onIconSelected(newIconId: String) {
        _uiState.update { it.copy(selectedIconId = newIconId) }
    }
    // ---------------------

    // --- NUEVOS INPUTS DE REPETICIÓN ---
    fun onRecurrenceModeChange(mode: RecurrenceMode) = _uiState.update { it.copy(recurrenceMode = mode) }

    fun onRecurrenceEndDateChange(date: LocalDate) = _uiState.update { it.copy(recurrenceEndDate = date) }

    fun onRecurrenceDayToggle(day: DayOfWeek) = _uiState.update {
        val currentDays = it.selectedRecurrenceDays.toMutableList()
        if (currentDays.contains(day)) currentDays.remove(day) else currentDays.add(day)
        it.copy(selectedRecurrenceDays = currentDays)
    }
    // -----------------------------------

    // --- ACCIÓN PRINCIPAL ---
    // --- FUNCIÓN CLAVE 2: GUARDAR ---
    fun saveTask() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val state = _uiState.value

            // Creamos la "Tarea Plantilla" con los datos del formulario
            val baseTask = Task(
                id = state.taskId ?: UUID.randomUUID().toString(),
                title = state.title,
                description = state.description,
                startTime = LocalDateTime(state.selectedDate, state.startTime),
                endTime = LocalDateTime(state.selectedDate, state.endTime),
                colorHex = state.selectedColorHex,
                iconId = state.selectedIconId,
                groupId = editingGroupId // Mantenemos el grupo si existe
            )

            val result = runCatching {
                when {
                    // CASO A: Estamos editando un GRUPO entero
                    editingGroupId != null -> {
                        updateTaskGroupUseCase(
                            groupId = editingGroupId!!,
                            templateTask = baseTask
                        )
                    }

                    // CASO B: Estamos creando una nueva tarea RECURRENTE
                    state.taskId == null && state.recurrenceMode != RecurrenceMode.ONCE -> {
                        createRecurringTaskUseCase(
                            baseTask = baseTask,
                            mode = state.recurrenceMode,
                            recurrenceEndDate = state.recurrenceEndDate,
                            selectedDays = state.selectedRecurrenceDays
                        )
                    }

                    // CASO C: Guardado normal (Crear simple o Editar individual)
                    else -> {
                        createTaskUseCase(
                            id = baseTask.id,
                            title = baseTask.title,
                            description = baseTask.description ?: "",
                            startTime = baseTask.startTime,
                            endTime = baseTask.endTime,
                            colorHex = baseTask.colorHex,
                            iconId = baseTask.iconId
                        )
                    }
                }
            }

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, isTaskSaved = true) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    // --- FUNCIÓN: CARGAR DATOS PARA EDITAR ---
    // --- FUNCIÓN CLAVE 1: PREPARAR EDICIÓN ---
    fun setTaskToEdit(task: Task?, isGroupEdit: Boolean = false) {
        if (task != null) {
            // Si el usuario eligió "Editar Todas", guardamos el ID del grupo.
            // Si eligió "Solo esta", editingGroupId será null.
            editingGroupId = if (isGroupEdit) task.groupId else null

            // Cargamos los datos en la UI
            _uiState.update {
                it.copy(
                    taskId = task.id,
                    title = task.title,
                    description = task.description ?: "",
                    selectedDate = task.startTime.date,
                    startTime = task.startTime.time,
                    endTime = task.endTime.time,
                    selectedColorHex = task.colorHex,
                    selectedIconId = task.iconId
                )
            }
        } else {
            // Modo crear nuevo
            editingGroupId = null
            resetState()
        }
    }


    fun resetState() { _uiState.value = CreateTaskUiState() }
}