package com.example.app_de_gestion_de_horarios_movil.di

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Room
import com.example.app_de_gestion_de_horarios_movil.data.local.AppDatabase
import com.example.app_de_gestion_de_horarios_movil.data.repository.AndroidAlarmScheduler
import com.example.app_de_gestion_de_horarios_movil.data.repository.TaskRepositoryImpl
import com.example.app_de_gestion_de_horarios_movil.data.repository.UserPreferencesRepositoryImpl
import com.example.app_de_gestion_de_horarios_movil.domain.repository.IAlarmScheduler
import com.example.app_de_gestion_de_horarios_movil.domain.repository.ITaskRepository
import com.example.app_de_gestion_de_horarios_movil.domain.repository.IUserPreferencesRepository
import com.example.app_de_gestion_de_horarios_movil.domain.usecase.*
import com.example.app_de_gestion_de_horarios_movil.ui.features.calendar.CalendarViewModel
import com.example.app_de_gestion_de_horarios_movil.ui.features.create_task.CreateTaskViewModel
import com.example.app_de_gestion_de_horarios_movil.ui.features.home.HomeViewModel
import com.example.app_de_gestion_de_horarios_movil.ui.features.settings.SettingsViewModel
import com.example.app_de_gestion_de_horarios_movil.ui.features.wizard.WizardViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@RequiresApi(Build.VERSION_CODES.O)
val appModule = module {

    // 1. BASE DE DATOS
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "cronologia_database.db"
        )
            .addMigrations(AppDatabase.MIGRATION_2_3)
            .build()
    }

    // 2. DAO
    single { get<AppDatabase>().taskDao() }

    // 3. REPOSITORIOS
    single<ITaskRepository> { TaskRepositoryImpl(get()) }
    single<IAlarmScheduler> { AndroidAlarmScheduler(androidContext()) }
    single<IUserPreferencesRepository> { UserPreferencesRepositoryImpl(androidContext()) }

    // 4. CASOS DE USO (Factories)
    factory { GetTasksForDateUseCase(get()) }
    factory { CreateTaskUseCase(get()) }
    factory { GenerateScheduleUseCase(get()) }
    factory { DeleteTaskUseCase(get()) }
    factory { ToggleTaskCompletionUseCase(get()) }
    factory { CreateRecurringTaskUseCase(get()) }
    factory { UpdateTaskGroupUseCase(get()) }
    factory { GetCalendarTasksUseCase(get()) }

    // 5. VIEWMODELS

    viewModel {
        HomeViewModel(
            getTasksForDateUseCase = get(),
            deleteTaskUseCase = get(),
            toggleTaskCompletionUseCase = get(),
            repository = get()
        )
    }

    // --- CORRECCIÓN 1: CreateTaskViewModel (5 Argumentos) ---
    // Quitamos Delete y Toggle porque este VM es solo para CREAR/EDITAR
    viewModel {
        CreateTaskViewModel(
            createTaskUseCase = get(),
            createRecurringTaskUseCase = get(),
            updateTaskGroupUseCase = get(),
            userPreferences = get(),
            alarmScheduler = get()
        )
    }

    // --- CORRECCIÓN 2: CalendarViewModel (3 Argumentos) ---
    // Agregamos Delete y Toggle porque la hoja de detalles del calendario los necesita
    viewModel {
        CalendarViewModel(
            getCalendarTasksUseCase = get(),     // 1. Obtener tareas
            deleteTaskUseCase = get(),           // 2. Eliminar
            toggleTaskCompletionUseCase = get()  // 3. Completar
        )
    }

    viewModel { SettingsViewModel(get()) }

    viewModel { WizardViewModel(get()) }
}