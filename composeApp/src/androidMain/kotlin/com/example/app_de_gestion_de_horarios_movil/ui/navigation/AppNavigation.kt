package com.example.app_de_gestion_de_horarios.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.app_de_gestion_de_horarios.ui.features.home.HomeScreen
import com.example.app_de_gestion_de_horarios.ui.features.wizard.WizardScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // 1. PANTALLA HOME
        composable(Screen.Home.route) {
            HomeScreen(
                // Pasamos la acción de navegar al Wizard
                onNavigateToWizard = {
                    navController.navigate(Screen.Wizard.route)
                }
            )
        }

        // 2. PANTALLA WIZARD
        composable(Screen.Wizard.route) {
            WizardScreen(
                // Cuando el Wizard termine, volvemos atrás (Home)
                onFinished = {
                    navController.popBackStack()
                }
            )
        }
    }
}