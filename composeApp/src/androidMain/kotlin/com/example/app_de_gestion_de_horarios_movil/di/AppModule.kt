package com.example.app_de_gestion_de_horarios_movil.di

import androidx.room.Room
import com.example.app_de_gestion_de_horarios_movil.data.local.AppDatabase
import com.example.app_de_gestion_de_horarios_movil.data.repository.TaskRepositoryImpl
import com.example.app_de_gestion_de_horarios_movil.domain.repository.ITaskRepository
import com.example.app_de_gestion_de_horarios_movil.domain.usecase.CreateTaskUseCase
import com.example.app_de_gestion_de_horarios_movil.domain.usecase.GenerateScheduleUseCase
import com.example.app_de_gestion_de_horarios_movil.domain.usecase.GetTasksForDateUseCase
import com.example.app_de_gestion_de_horarios_movil.ui.features.create_task.CreateTaskViewModel
import com.example.app_de_gestion_de_horarios_movil.ui.features.home.HomeViewModel
import com.example.app_de_gestion_de_horarios_movil.ui.features.wizard.WizardViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // 1. BASE DE DATOS (Singleton)
    // Crea la instancia de la base de datos una sola vez.
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "cronologia_database.db" // Nombre del archivo físico
        )
            .fallbackToDestructiveMigration() // Si cambias el esquema, borra y crea de nuevo (útil en dev)
            .build()
    }

    // 2. DAO
    // Pide la instancia de Database (definida arriba) y extrae el DAO.
    // 'get()' busca automáticamente la definición de AppDatabase en este módulo.
    single {
        get<AppDatabase>().taskDao()
    }

    // 3. REPOSITORIO
    // Conecta la Interfaz con la Implementación.
    // Cuando alguien pida 'ITaskRepository', Koin le dará un 'TaskRepositoryImpl'.
    // El 'get()' inyecta automáticamente el DAO necesario.
    single<ITaskRepository> {
        TaskRepositoryImpl(dao = get())
    }

    // 4. USE CASES
    // Factory: Crea una instancia nueva cada vez que se pide (no guarda estado)
    factory { GetTasksForDateUseCase(repository = get()) }

    // 5. VIEW MODELS
    // viewModel: Koin maneja el ciclo de vida de Android automáticamente
    viewModel { HomeViewModel(getTasksForDateUseCase = get()) }


    // NUEVO UseCase
    factory { CreateTaskUseCase(repository = get()) }

    // NUEVO ViewModel
    viewModel { CreateTaskViewModel(createTaskUseCase = get()) }


    // Wizard
    factory { GenerateScheduleUseCase(repository = get()) }
    viewModel { WizardViewModel(generateScheduleUseCase = get()) }
}