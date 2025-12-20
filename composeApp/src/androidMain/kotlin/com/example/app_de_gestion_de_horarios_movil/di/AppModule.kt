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
    // ¡IMPORTANTE!: Solo debe haber UNA definición 'single' para la base de datos.
    // He fusionado tus dos bloques: usa el nombre original pero AGREGA la migración.
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "cronologia_database.db" // Mantén el nombre que ya usabas para no perder datos previos
        )
            .addMigrations(AppDatabase.MIGRATION_1_2) // <-- Agregamos la migración Fase 3
            .build()
    }

    // 2. DAO
    single { get<AppDatabase>().taskDao() }

    // 3. REPOSITORIOS
    single<ITaskRepository> { TaskRepositoryImpl(get()) }

    // PROGRAMADOR DE ALARMAS
    single<IAlarmScheduler> { AndroidAlarmScheduler(androidContext()) }

    // Repositorio de Ajustes (Singleton para DataStore)
    single<IUserPreferencesRepository> {
        UserPreferencesRepositoryImpl(androidContext())
    }



    // 4. CASOS DE USO
    factory { GetTasksForDateUseCase(get()) }
    factory { CreateTaskUseCase(get()) }
    factory { GenerateScheduleUseCase(get()) }
    factory { DeleteTaskUseCase(get()) }
    factory { ToggleTaskCompletionUseCase(get()) }
    factory { CreateRecurringTaskUseCase(get()) }
    factory { UpdateTaskGroupUseCase(get()) }

    // 5. VIEWMODELS

    viewModel {
        HomeViewModel(
            getTasksForDateUseCase = get(),
            deleteTaskUseCase = get(),
            toggleTaskCompletionUseCase = get(),
            repository = get()
            // Nota: HomeViewModel no necesita userPreferences por ahora,
            // a menos que lo hayas modificado también. Si da error, bórralo de aquí.
        )
    }

    // --- CORRECCIÓN AQUÍ ---
    viewModel {
        CreateTaskViewModel(
            createTaskUseCase = get(),
            createRecurringTaskUseCase = get(),
            updateTaskGroupUseCase = get(),
            userPreferences = get(), // <--- ¡FALTABA ESTO! Inyectamos el repo de preferencias
            alarmScheduler = get()
        )
    }

    viewModel { SettingsViewModel(get()) }

    viewModel { WizardViewModel(get()) }
}