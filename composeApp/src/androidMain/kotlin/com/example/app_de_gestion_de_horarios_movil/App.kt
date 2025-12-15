package com.example.app_de_gestion_de_horarios_movil

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
// Asegúrate de importar tu HomeScreen
import com.example.app_de_gestion_de_horarios.ui.features.home.HomeScreen
import org.koin.compose.KoinContext

@RequiresApi(Build.VERSION_CODES.O)
@Composable
@Preview
fun App() {
    MaterialTheme {
        // KoinContext es necesario en KMP para que la inyección funcione en Compose
        // Si solo estás en Android puro, no es estrictamente necesario, pero es buena práctica en KMP
        KoinContext {
            HomeScreen()
        }
    }
}