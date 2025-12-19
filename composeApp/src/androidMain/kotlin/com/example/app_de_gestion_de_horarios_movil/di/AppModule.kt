package com.example.app_de_gestion_de_horarios_movil.di

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.app_de_gestion_de_horarios_movil.data.local.AppDatabase
import com.example.app_de_gestion_de_horarios_movil.data.repository.TaskRepositoryImpl
import com.example.app_de_gestion_de_horarios_movil.domain.repository.ITaskRepository
import com.example.app_de_gestion_de_horarios_movil.domain.usecase.* import com.example.app_de_gestion_de_horarios_movil.ui.features.create_task.CreateTaskViewModel
import com.example.app_de_gestion_de_horarios_movil.ui.features.home.HomeViewModel
import com.example.app_de_gestion_de_horarios_movil.ui.features.wizard.WizardViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import androidx.room.Room
import com.example.app_de_gestion_de_horarios_movil.data.repository.UserPreferencesRepositoryImpl
import com.example.app_de_gestion_de_horarios_movil.domain.repository.IUserPreferencesRepository

@RequiresApi(Build.VERSION_CODES.O)
val appModule = module {

    // 1. BASE DE DATOS
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "cronologia_database.db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    // 2. DAO
    single { get<AppDatabase>().taskDao() }

    // 3. REPOSITORIO
    single<ITaskRepository> { TaskRepositoryImpl(get()) }

    // --- NUEVO: PREFERENCIAS ---
    // Repositorio de Ajustes (Singleton porque DataStore debe ser único)
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
        )
    }

    // Actualizar el ViewModel para recibirlo
    viewModel {
        CreateTaskViewModel(
            createTaskUseCase = get(),
            createRecurringTaskUseCase = get(),
            updateTaskGroupUseCase = get() // <--- AGREGAR ESTO AL CONSTRUCTOR DEL VM
        )
    }



    viewModel { WizardViewModel(get()) }
}