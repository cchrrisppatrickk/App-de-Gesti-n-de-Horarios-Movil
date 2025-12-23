package com.example.app_de_gestion_de_horarios_movil.domain.usecase

import com.example.app_de_gestion_de_horarios_movil.domain.model.Task
import com.example.app_de_gestion_de_horarios_movil.domain.repository.ITaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.* // Importar todo lo de kotlinx

class GetCalendarTasksUseCase(
    private val repository: ITaskRepository
) {
    /**
     * Obtiene las tareas para un mes específico.
     * @param year Año (ej. 2025)
     * @param month Mes (ej. Month.OCTOBER)
     */

    operator fun invoke(year: Int, month: Month): Flow<Map<LocalDate, List<Task>>> {
        val startDate = LocalDate(year, month, 1)

        // CORRECCIÓN: Agregar un "colchón" de 7 días antes y después
        // para asegurar que las celdas grises del principio y fin del grid tengan datos.

        val startWithPadding = startDate.minus(DatePeriod(days = 7))

        val endDate = startDate
            .plus(DatePeriod(months = 1)) // Mes siguiente
            .plus(DatePeriod(days = 7))   // + 7 días del siguiente mes

        return repository.getTasksBetweenDates(startWithPadding, endDate)
    }

//    operator fun invoke(year: Int, month: Month): Flow<Map<LocalDate, List<Task>>> {
//        // 1. Calcular el primer día del mes (1 de [Mes])
//        val startDate = LocalDate(year, month, 1)
//
//        // 2. Calcular el último día del mes.
//        // En kotlinx-datetime no existe "atEndOfMonth".
//        // Lógica: Sumamos 1 mes a la fecha de inicio y restamos 1 día.
//        val endDate = startDate
//            .plus(DatePeriod(months = 1))
//            .minus(DatePeriod(days = 1))
//
//        // Delegar al repositorio con tipos kotlinx.datetime puros
//        return repository.getTasksBetweenDates(startDate, endDate)
//    }

    // Sobrecarga opcional: Si la UI te pasa una fecha actual y quieres el mes de esa fecha
    operator fun invoke(currentDate: LocalDate): Flow<Map<LocalDate, List<Task>>> {
        return invoke(currentDate.year, currentDate.month)
    }
}