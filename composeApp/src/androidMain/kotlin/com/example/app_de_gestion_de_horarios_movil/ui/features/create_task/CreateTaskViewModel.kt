package com.example.app_de_gestion_de_horarios_movil.ui.features.create_task

import TaskType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_de_gestion_de_horarios_movil.domain.model.NotificationType
import com.example.app_de_gestion_de_horarios_movil.domain.model.RecurrenceMode
import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.domain.repository.IAlarmScheduler
import com.example.app_de_gestion_de_horarios_movil.domain.repository.IUserPreferencesRepository
import com.example.app_de_gestion_de_horarios_movil.domain.usecase.CreateRecurringTaskUseCase
import com.example.app_de_gestion_de_horarios_movil.domain.usecase.CreateTaskUseCase
import com.example.app_de_gestion_de_horarios_movil.domain.usecase.UpdateTaskGroupUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    private val updateTaskGroupUseCase: UpdateTaskGroupUseCase,
    // --- INYECCIÓN DE DEPENDENCIAS ---
    private val userPreferences: IUserPreferencesRepository,
    private val alarmScheduler: IAlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTaskUiState())
    val uiState = _uiState.asStateFlow()

    private var editingGroupId: String? = null

    // --- EVENTOS DE ENTRADA (INPUTS) ---

    fun onTitleChange(newTitle: String) = _uiState.update { it.copy(title = newTitle) }
    fun onDescriptionChange(newDesc: String) = _uiState.update { it.copy(description = newDesc) }
    fun onDateChange(newDate: LocalDate) = _uiState.update { it.copy(selectedDate = newDate) }
    fun onStartTimeChange(newTime: LocalTime) = _uiState.update { it.copy(startTime = newTime) }
    fun onEndTimeChange(newTime: LocalTime) = _uiState.update { it.copy(endTime = newTime) }
    fun onColorSelected(newColorHex: String) = _uiState.update { it.copy(selectedColorHex = newColorHex) }
    fun onIconSelected(newIconId: String) = _uiState.update { it.copy(selectedIconId = newIconId) }

    // --- NUEVOS EVENTOS DE ENTRADA ---
    fun setEntryType(type: TaskType) = _uiState.update { it.copy(entryType = type) }

    fun onAllDayToggle(isAllDay: Boolean) {
        _uiState.update {
            // Si activa "Todo el día", podríamos resetear horas, pero mantenerlas guardadas es mejor UX
            it.copy(isAllDay = isAllDay)
        }
    }
    // ---------------------------------

    // Recurrencia
    fun onRecurrenceModeChange(mode: RecurrenceMode) = _uiState.update { it.copy(recurrenceMode = mode) }
    fun onRecurrenceEndDateChange(date: LocalDate) = _uiState.update { it.copy(recurrenceEndDate = date) }
    fun onRecurrenceDayToggle(day: DayOfWeek) = _uiState.update {
        val currentDays = it.selectedRecurrenceDays.toMutableList()
        if (currentDays.contains(day)) currentDays.remove(day) else currentDays.add(day)
        it.copy(selectedRecurrenceDays = currentDays)
    }

    // --- GESTIÓN DE ALERTAS (CHECKBOXES) ---
    fun onAlertToggle(type: NotificationType) {
        _uiState.update { state ->
            val current = state.selectedAlerts.toMutableList()
            if (current.contains(type)) {
                current.remove(type)
            } else {
                current.add(type)
            }
            state.copy(selectedAlerts = current)
        }
    }


    // --- ACCIÓN PRINCIPAL: GUARDAR ---
    fun saveTask() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val state = _uiState.value

            // Construimos el objeto Task "Plantilla" con todos los datos del formulario
            val baseTask = Task(
                id = state.taskId ?: UUID.randomUUID().toString(),
                title = state.title,
                description = state.description,
                startTime = LocalDateTime(state.selectedDate, state.startTime),
                endTime = LocalDateTime(state.selectedDate, state.endTime),
                colorHex = state.selectedColorHex,
                iconId = state.selectedIconId,
                groupId = editingGroupId,
                // Importante: Pasamos la lista de alertas seleccionadas al modelo
                activeAlerts = state.selectedAlerts,
                // --- NUEVOS CAMPOS PROPAGADOS ---
                type = state.entryType,
                isAllDay = state.isAllDay
                // --------------------------------
            )

            val result = runCatching {
                when {
                    // CASO A: Edición de GRUPO (Recurrente existente)
                    editingGroupId != null -> {
                        updateTaskGroupUseCase(
                            groupId = editingGroupId!!,
                            templateTask = baseTask
                        )
                        // TODO: Idealmente aquí deberíamos reprogramar las alarmas para todo el grupo.
                        // Esto requeriría que el UseCase retorne los IDs afectados o mover el scheduler al UseCase.
                    }

                    // CASO B: Creación de NUEVA tarea RECURRENTE
                    state.taskId == null && state.recurrenceMode != RecurrenceMode.ONCE -> {
                        createRecurringTaskUseCase(
                            baseTask = baseTask,
                            mode = state.recurrenceMode,
                            recurrenceEndDate = state.recurrenceEndDate,
                            selectedDays = state.selectedRecurrenceDays
                        )
                        // TODO: Similar al caso A, el scheduler debería ejecutarse para cada tarea generada.
                    }

                    // CASO C: Tarea SIMPLE (Crear nueva o Editar individual)
                    else -> {
                        // 1. Guardar en Base de Datos
                        createTaskUseCase(
                            id = baseTask.id,
                            title = baseTask.title,
                            description = baseTask.description ?: "",
                            startTime = baseTask.startTime,
                            endTime = baseTask.endTime,
                            colorHex = baseTask.colorHex,
                            iconId = baseTask.iconId,
                            activeAlerts = baseTask.activeAlerts, // Pasamos las alertas a la BD

                            type = baseTask.type,
                            isAllDay = baseTask.isAllDay
                        )

                        // 2. Programar la Alarma en el Sistema (Android AlarmManager)
                        // Solo programamos si la operación de BD fue exitosa
                        alarmScheduler.schedule(baseTask)
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

    // --- CARGAR DATOS (PARA EDITAR O INICIAR) ---
    fun setTaskToEdit(task: Task?, isGroupEdit: Boolean = false) {
        if (task != null) {
            // MODO EDICIÓN: Cargamos los datos existentes de la tarea
            editingGroupId = if (isGroupEdit) task.groupId else null

            _uiState.update {
                it.copy(
                    taskId = task.id,
                    title = task.title,
                    description = task.description ?: "",
                    selectedDate = task.startTime.date,
                    startTime = task.startTime.time,
                    endTime = task.endTime.time,
                    selectedColorHex = task.colorHex,
                    selectedIconId = task.iconId,
                    // Cargamos las alertas que ya tenía la tarea guardada
                    selectedAlerts = task.activeAlerts,
                    // --- CARGAR NUEVOS CAMPOS ---
                    entryType = task.type,
                    isAllDay = task.isAllDay
                    // ----------------------------
                )
            }
        } else {
            // MODO CREAR NUEVO: Reiniciamos formulario y cargamos DEFAULTS
            editingGroupId = null
            resetState()
            loadDefaultAlerts()
        }
    }

    fun resetState() { _uiState.value = CreateTaskUiState() }

    // --- LÓGICA DE PREFERENCIAS ---
    // Carga los valores por defecto definidos en Ajustes (DataStore)
    private fun loadDefaultAlerts() {
        viewModelScope.launch {
            val settings = userPreferences.userSettings.first()

            // Si el "Master Switch" de notificaciones está apagado, no marcamos nada por defecto
            if (!settings.areNotificationsEnabled) return@launch

            val defaults = mutableListOf<NotificationType>()
            if (settings.notifyAtStart) defaults.add(NotificationType.AT_START)
            if (settings.notifyAtEnd) defaults.add(NotificationType.AT_END)
            if (settings.notify15MinutesBefore) defaults.add(NotificationType.FIFTEEN_MIN_BEFORE)

            _uiState.update { it.copy(selectedAlerts = defaults) }
        }
    }
}