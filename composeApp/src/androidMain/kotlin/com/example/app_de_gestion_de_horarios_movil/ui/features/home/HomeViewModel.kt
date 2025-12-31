package com.example.app_de_gestion_de_horarios_movil.ui.features.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.domain.repository.ITaskRepository
import com.example.app_de_gestion_de_horarios_movil.domain.usecase.DeleteTaskUseCase
import com.example.app_de_gestion_de_horarios_movil.domain.usecase.GetTasksForDateUseCase
import com.example.app_de_gestion_de_horarios_movil.domain.usecase.ToggleTaskCompletionUseCase
import com.example.app_de_gestion_de_horarios_movil.ui.components.CalendarIndicator // Asegúrate de importar esto
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
class HomeViewModel(
    private val getTasksForDateUseCase: GetTasksForDateUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase,
    private val repository: ITaskRepository,
) : ViewModel() {

    // --- ESTADOS ---

    // Control del Sheet de detalles
    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask = _selectedTask.asStateFlow()

    // Estado de la UI principal
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // CAMBIO CRUCIAL: Ahora es una lista de CalendarIndicator, no de Strings
    private val _calendarColors = MutableStateFlow<Map<LocalDate, List<CalendarIndicator>>>(emptyMap())
    val calendarColors = _calendarColors.asStateFlow()

    // Rango del calendario
    val calendarStartDate: LocalDate
    val calendarEndDate: LocalDate

    // Control de suscripción
    private var getTasksJob: Job? = null

    // --- INICIALIZACIÓN ---
    init {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        // 1. Definimos la ventana de tiempo (6 meses atrás/adelante)
        calendarStartDate = today.minus(DatePeriod(months = 6))
        calendarEndDate = today.plus(DatePeriod(months = 6))

        // 2. Cargamos datos iniciales
        onDateSelected(today)
        loadCalendarIndicators()
    }

    // --- CARGA DE DATOS ---

    private fun loadCalendarIndicators() {
        viewModelScope.launch {
            // El repositorio devuelve TaskIndicator (Domain), lo transformamos a CalendarIndicator (UI)
            repository.getCalendarIndicators(calendarStartDate, calendarEndDate)
                .collect { domainMap ->
                    val uiMap = domainMap.mapValues { entry ->
                        entry.value.map { domainIndicator ->
                            CalendarIndicator(
                                colorHex = domainIndicator.colorHex,
                                type = domainIndicator.type
                            )
                        }
                    }
                    _calendarColors.value = uiMap
                }
        }
    }

    /**
     * Evento: Cuando el usuario toca un día en el calendario
     */
    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date, isLoading = true) }
        getTasksJob?.cancel()

        getTasksJob = getTasksForDateUseCase(date)
            .onEach { tasks ->
                val timelineItems = transformTasksToTimelineItems(tasks)

                _uiState.update {
                    it.copy(
                        tasks = tasks,
                        timelineItems = timelineItems,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    // --- GESTIÓN DE TAREAS (Acciones del Sheet) ---

    fun onTaskSelected(task: Task) {
        _selectedTask.value = task
    }

    fun onDismissTaskDetails() {
        _selectedTask.value = null
    }

    fun onDeleteTask() {
        val task = _selectedTask.value ?: return
        viewModelScope.launch {
            deleteTaskUseCase(task.id)
            onDismissTaskDetails()
        }
    }

    fun onDeleteAllOccurrences() {
        val task = _selectedTask.value ?: return
        val groupId = task.groupId ?: return

        viewModelScope.launch {
            deleteTaskUseCase.deleteGroup(groupId)
            onDismissTaskDetails()
        }
    }

    fun onToggleCompletion(task: Task) {
        viewModelScope.launch {
            toggleTaskCompletionUseCase(task)
            // Actualizamos la tarea seleccionada en tiempo real si está abierta
            if (_selectedTask.value?.id == task.id) {
                _selectedTask.value = task.copy(isCompleted = !task.isCompleted)
            }
        }
    }

    // La lógica de navegación para editar se maneja en la UI observando el evento
    fun onEditTask() {
        onDismissTaskDetails()
    }

    // --- TRANSFORMACIÓN DE DATOS (Timeline con Huecos) ---

    private fun transformTasksToTimelineItems(tasks: List<Task>): List<TimelineItem> {
        if (tasks.isEmpty()) return emptyList()

        val result = mutableListOf<TimelineItem>()
        val sortedTasks = tasks.sortedBy { it.startTime }

        for (i in sortedTasks.indices) {
            val currentTask = sortedTasks[i]

            // 1. Tarea
            result.add(TimelineItem.TaskItem(currentTask))

            // 2. Hueco con la siguiente
            if (i < sortedTasks.lastIndex) {
                val nextTask = sortedTasks[i + 1]
                val endCurrent = currentTask.endTime.toJavaLocalDateTime()
                val startNext = nextTask.startTime.toJavaLocalDateTime()

                val diffMinutes = ChronoUnit.MINUTES.between(endCurrent, startNext)

                if (diffMinutes >= 15) {
                    result.add(
                        TimelineItem.GapItem(
                            start = endCurrent,
                            end = startNext,
                            durationMinutes = diffMinutes
                        )
                    )
                }
            }
        }
        return result
    }
}