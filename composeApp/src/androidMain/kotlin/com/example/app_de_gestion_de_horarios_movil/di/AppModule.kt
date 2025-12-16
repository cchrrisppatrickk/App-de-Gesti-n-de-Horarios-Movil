package com.example.app_de_gestion_de_horarios_movil.di

import com.example.app_de_gestion_de_horarios_movil.data.local.AppDatabase
import com.example.app_de_gestion_de_horarios_movil.data.repository.TaskRepositoryImpl
import com.example.app_de_gestion_de_horarios_movil.domain.repository.ITaskRepository
import com.example.app_de_gestion_de_horarios_movil.domain.usecase.* // Importa todos los UseCases
import com.example.app_de_gestion_de_horarios_movil.ui.features.create_task.CreateTaskViewModel
import com.example.app_de_gestion_de_horarios_movil.ui.features.home.HomeViewModel
import com.example.app_de_gestion_de_horarios_movil.ui.features.wizard.WizardViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import androidx.room.Room

val appModule = module {

    // 1. BASE DE DATOS (Singleton)
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "cronologia_database.db"
        ).fallbackToDestructiveMigration() // Útil en desarrollo si cambias modelos
            .build()
    }

    // 2. DAO
    single { get<AppDatabase>().taskDao() }

    // 3. REPOSITORIO
    single<ITaskRepository> { TaskRepositoryImpl(get()) }

    // 4. CASOS DE USO (Use Cases)
    factory { GetTasksForDateUseCase(get()) }
    factory { CreateTaskUseCase(get()) }
    factory { GenerateScheduleUseCase(get()) }

    // --- AGREGAR ESTOS DOS NUEVOS ---
    factory { DeleteTaskUseCase(get()) }
    factory { ToggleTaskCompletionUseCase(get()) }

    // 5. VIEWMODELS

    // Aquí estaba el error. Koin necesita saber cómo llenar los nuevos parámetros.
    viewModel {
        HomeViewModel(
            getTasksForDateUseCase = get(),
            deleteTaskUseCase = get(),          // <--- NUEVO
            toggleTaskCompletionUseCase = get() // <--- NUEVO
        )
    }

    viewModel { CreateTaskViewModel(get()) }

    viewModel { WizardViewModel(get()) }
}