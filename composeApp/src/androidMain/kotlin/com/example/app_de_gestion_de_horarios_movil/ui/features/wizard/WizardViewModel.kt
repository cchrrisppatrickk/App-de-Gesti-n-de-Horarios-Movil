package com.example.app_de_gestion_de_horarios.ui.features.wizard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_de_gestion_de_horarios.domain.model.SubjectConfig
import com.example.app_de_gestion_de_horarios.domain.model.WeeklySchedule
import com.example.app_de_gestion_de_horarios.domain.usecase.GenerateScheduleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

// Estado de la UI del Wizard
data class WizardUiState(
    val startDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val endDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.plus(kotlinx.datetime.DatePeriod(months = 4)), // Por defecto 4 meses

    val subjects: List<SubjectConfig> = emptyList(), // Lista temporal de materias

    val isGenerating: Boolean = false,
    val isFinished: Boolean = false,
    val generatedCount: Int = 0
)

class WizardViewModel(
    private val generateScheduleUseCase: GenerateScheduleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WizardUiState())
    val uiState = _uiState.asStateFlow()

    // 1. Configurar Fechas
    fun onStartDateChange(date: LocalDate) {
        _uiState.update { it.copy(startDate = date) }
    }
    fun onEndDateChange(date: LocalDate) {
        _uiState.update { it.copy(endDate = date) }
    }

    // 2. Agregar una Materia a la lista temporal
    fun addSubject(name: String, color: String, days: List<DayOfWeek>, start: LocalTime, end: LocalTime) {
        // Convertimos los días seleccionados (Ej: Lunes, Miercoles) en objetos Schedule
        val schedules = days.map { day ->
            WeeklySchedule(day, start, end)
        }

        val newSubject = SubjectConfig(
            id = UUID.randomUUID().toString(),
            name = name,
            colorHex = color,
            schedules = schedules
        )

        _uiState.update {
            it.copy(subjects = it.subjects + newSubject)
        }
    }

    fun removeSubject(subjectId: String) {
        _uiState.update {
            it.copy(subjects = it.subjects.filter { s -> s.id != subjectId })
        }
    }

    // 3. ¡GENERAR! (El botón final)
    fun generateSchedule() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }

            val result = generateScheduleUseCase(
                startDate = _uiState.value.startDate,
                endDate = _uiState.value.endDate,
                subjects = _uiState.value.subjects
            )

            result.onSuccess { count ->
                _uiState.update { it.copy(isGenerating = false, isFinished = true, generatedCount = count) }
            }.onFailure {
                _uiState.update { it.copy(isGenerating = false) } // Manejar error aquí
            }
        }
    }
}