package com.example.app_de_gestion_de_horarios_movil.ui.features.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.domain.usecase.DeleteTaskUseCase
import com.example.app_de_gestion_de_horarios_movil.domain.usecase.GetCalendarTasksUseCase
import com.example.app_de_gestion_de_horarios_movil.domain.usecase.ToggleTaskCompletionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.*

class CalendarViewModel(
    private val getCalendarTasksUseCase: GetCalendarTasksUseCase,
    // --- NUEVAS DEPENDENCIAS INYECTADAS ---
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase

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
        val currentMonthRef = _uiState.value.currentMonth
        // Normalizamos la nueva fecha al día 1 para comparar solo AÑO y MES
        val newMonthRef = LocalDate(date.year, date.month, 1)

        if (currentMonthRef != newMonthRef) {
            // 1. CAMBIO DE MES DETECTADO
            _uiState.update {
                it.copy(
                    selectedDate = date,
                    currentMonth = newMonthRef // Actualizamos el mes de referencia
                )
            }
            // 2. IMPORTANTE: Recargar datos para el nuevo mes
            loadTasksForCurrentMonth()
        } else {
            // Mismo mes, solo cambiamos la selección (no hace falta recargar DB)
            _uiState.update { it.copy(selectedDate = date) }
        }
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

    // --- FUNCIONES QUE FALTABAN (SOLUCIÓN AL ERROR) ---

    fun onDeleteTask(task: Task) {
        viewModelScope.launch {
            // Aquí podrías añadir lógica para preguntar si borrar solo esta o toda la serie
            // Por ahora, borramos la instancia específica (invoke operator)
            deleteTaskUseCase(task.id)

            // Nota: Al borrar, el Flow de getCalendarTasksUseCase se actualizará solo
            // y la UI refrescará la lista automáticamente.
        }
    }

    fun onToggleCompletion(task: Task) {
        viewModelScope.launch {
            toggleTaskCompletionUseCase(task)
        }
    }

    fun onDeleteAllOccurrences(task: Task) {
        val groupId = task.groupId ?: return
        viewModelScope.launch {
            // Llamamos al método deleteGroup del UseCase
            deleteTaskUseCase.deleteGroup(groupId)
            // El Flow de tareas se actualizará automáticamente
        }
    }

    /// iu cambio del mes


}