package com.example.app_de_gestion_de_horarios.ui.features.create_task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_de_gestion_de_horarios.domain.usecase.CreateTaskUseCase
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

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onDescriptionChange(newDesc: String) {
        _uiState.update { it.copy(description = newDesc) }
    }

    fun onDateChange(newDate: LocalDate) {
        _uiState.update { it.copy(selectedDate = newDate) }
    }

    fun onStartTimeChange(newTime: LocalTime) {
        _uiState.update { it.copy(startTime = newTime) }
    }

    fun onEndTimeChange(newTime: LocalTime) {
        _uiState.update { it.copy(endTime = newTime) }
    }

    // --- ACCIÓN PRINCIPAL ---

    fun saveTask() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val state = _uiState.value

            // Combinar Fecha + Hora para crear LocalDateTime
            val startDateTime = LocalDateTime(state.selectedDate, state.startTime)
            val endDateTime = LocalDateTime(state.selectedDate, state.endTime)

            val result = createTaskUseCase(
                title = state.title,
                description = state.description,
                startTime = startDateTime,
                endTime = endDateTime
            )

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, isTaskSaved = true) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    // Resetear estado (útil al reabrir el modal)
    fun resetState() {
        _uiState.value = CreateTaskUiState()
    }
}