package com.example.app_de_gestion_de_horarios_movil.ui.features.create_task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.domain.usecase.CreateTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

class CreateTaskViewModel(
    private val createTaskUseCase: CreateTaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTaskUiState())
    val uiState = _uiState.asStateFlow()

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

    // --- ACCIÓN PRINCIPAL ---
    fun saveTask() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val state = _uiState.value

            val startDateTime = LocalDateTime(state.selectedDate, state.startTime)
            val endDateTime = LocalDateTime(state.selectedDate, state.endTime)

            val result = createTaskUseCase(
                id = state.taskId, // <--- Pasamos el ID (puede ser null o existente)
                title = state.title,
                description = state.description,
                startTime = startDateTime,
                endTime = endDateTime,
                // Pasamos los nuevos datos
                colorHex = state.selectedColorHex,
                iconId = state.selectedIconId
            )

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, isTaskSaved = true) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    // --- NUEVA FUNCIÓN: CARGAR DATOS PARA EDITAR ---
    fun setTaskToEdit(task: Task?) {
        if (task != null) {
            // Modo Edición: Llenamos el estado con los datos de la tarea
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
            // Modo Creación: Reseteamos a valores por defecto
            resetState()
        }
    }
    fun resetState() { _uiState.value = CreateTaskUiState() }
}