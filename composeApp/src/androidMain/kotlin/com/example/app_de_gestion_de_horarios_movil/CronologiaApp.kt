package com.example.app_de_gestion_de_horarios_movil

import android.app.Application
import com.example.app_de_gestion_de_horarios_movil.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class CronologiaApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // INICIAR KOIN
        startKoin {
            // Log de errores de Koin (útil para debug)
            androidLogger()
            // Contexto de Android (necesario para crear la DB de Room)
            androidContext(this@CronologiaApp)
            // Cargamos nuestros módulos
            modules(appModule)
        }
    }
}