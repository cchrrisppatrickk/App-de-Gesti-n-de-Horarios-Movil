package com.example.app_de_gestion_de_horarios_movil.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_de_gestion_de_horarios_movil.domain.usecase.GetTasksForDateUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class HomeViewModel(
    private val getTasksForDateUseCase: GetTasksForDateUseCase
) : ViewModel() {

    // Estado mutable privado (solo el VM lo modifica)
    private val _uiState = MutableStateFlow(HomeUiState())
    // Estado público de solo lectura (la UI lo observa)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Variable para controlar la suscripción al Flow (evita fugas de memoria)
    private var getTasksJob: Job? = null

    init {
        // Al iniciar, cargar la fecha de hoy
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        onDateSelected(today)
    }

    /**
     * Evento: Cuando el usuario toca un día en el calendario
     */
    fun onDateSelected(date: LocalDate) {
        // 1. Actualizamos la fecha en el estado
        _uiState.update { it.copy(selectedDate = date, isLoading = true) }

        // 2. Cancelamos la observación anterior (si cambiamos de día rápido)
        getTasksJob?.cancel()

        // 3. Nos suscribimos al flujo de tareas de la nueva fecha
        getTasksJob = getTasksForDateUseCase(date)
            .onEach { tasks ->
                _uiState.update {
                    it.copy(
                        tasks = tasks,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}