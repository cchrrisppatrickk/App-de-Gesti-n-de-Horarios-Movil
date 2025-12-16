package com.example.app_de_gestion_de_horarios_movil // O tu paquete raíz

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.example.app_de_gestion_de_horarios.ui.navigation.AppNavigation // Importar navegación
import org.koin.compose.KoinContext

@RequiresApi(Build.VERSION_CODES.O)
@Composable
@Preview
fun App() {
    MaterialTheme {
        // Envolvemos todo en KoinContext para asegurar la inyección
        KoinContext {
            // EN LUGAR DE HOMESCREEN(), LLAMAMOS A LA NAVEGACIÓN
            AppNavigation()
        }
    }
}