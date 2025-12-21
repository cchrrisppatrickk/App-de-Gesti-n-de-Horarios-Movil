package com.example.app_de_gestion_de_horarios_movil.ui.features.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_de_gestion_de_horarios_movil.domain.usecase.GetCalendarTasksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.*

class CalendarViewModel(
    private val getCalendarTasksUseCase: GetCalendarTasksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CalendarUiState(
            currentMonth = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.let {
                LocalDate(it.year, it.month, 1) // Inicializar en el día 1 del mes actual
            },
            selectedDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        )
    )
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadTasksForCurrentMonth()
    }

    private fun loadTasksForCurrentMonth() {
        val currentRef = _uiState.value.currentMonth

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Llamamos al UseCase con Año y Mes
            getCalendarTasksUseCase(currentRef.year, currentRef.month)
                .collect { tasksMap ->
                    _uiState.update {
                        it.copy(
                            tasks = tasksMap,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun onNextMonth() {
        _uiState.update { currentState ->
            currentState.copy(
                currentMonth = currentState.currentMonth.plus(DatePeriod(months = 1))
            )
        }
        loadTasksForCurrentMonth()
    }

    fun onPreviousMonth() {
        _uiState.update { currentState ->
            currentState.copy(
                currentMonth = currentState.currentMonth.minus(DatePeriod(months = 1))
            )
        }
        loadTasksForCurrentMonth()
    }
}