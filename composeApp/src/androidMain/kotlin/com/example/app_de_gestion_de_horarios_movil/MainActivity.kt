package com.example.app_de_gestion_de_horarios_movil

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
// 1. IMPORTANTE: Asegúrate de importar tu tema (el nombre puede variar según tu archivo Theme.kt)
import com.example.app_de_gestion_de_horarios_movil.ui.theme.AppDeGestionDeHorariosMovilTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            // 2. CORRECCIÓN: Envuelve tu App() con tu Tema
            AppDeGestionDeHorariosMovilTheme {
                App()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun AppAndroidPreview() {
    // 3. También en el Preview para que veas los colores en el editor
    AppDeGestionDeHorariosMovilTheme {
        App()
    }
}