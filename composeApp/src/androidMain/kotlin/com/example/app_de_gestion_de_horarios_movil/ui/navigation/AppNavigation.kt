package com.example.app_de_gestion_de_horarios_movil.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.app_de_gestion_de_horarios_movil.ui.features.home.HomeScreen
import com.example.app_de_gestion_de_horarios_movil.ui.features.wizard.WizardScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Lista de pantallas que aparecerán en la barra inferior
    val bottomNavItems = listOf(
        Screen.Calendario,
        Screen.Tareas,
        Screen.Horarios,
        Screen.Mios
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                // Obtenemos la ruta actual para saber qué icono pintar activo
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Lógica de navegación recomendada por Google:

                                // 1. Pop hasta el inicio para evitar pilas gigantes al cambiar de tabs
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // 2. Evitar copias múltiples de la misma pantalla si das click repetido
                                launchSingleTop = true
                                // 3. Restaurar estado (scroll, etc) al volver
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // El contenedor principal
        NavHost(
            navController = navController,
            startDestination = Screen.Calendario.route,
            modifier = Modifier.padding(innerPadding) // ¡Importante! Respetar el padding de la barra
        ) {

            // 1. CALENDARIO (Tu Home/Timeline)
            composable(Screen.Calendario.route) {
                PlaceholderScreen("Calendario")
            }

            // 2. TAREAS (Placeholder)
            composable(Screen.Tareas.route) {
                HomeScreen(
                    // Si desde el calendario quieres ir directo a editar horarios
                    onNavigateToWizard = { navController.navigate(Screen.Horarios.route) }
                )
            }

            // 3. HORARIOS (Tu Wizard)
            composable(Screen.Horarios.route) {
                // Aquí mostramos el Wizard.
                // Nota: Si el Wizard tiene su propio flujo interno, podrías necesitar ajustar esto,
                // pero para "ver" los horarios, esta ruta está bien.
                WizardScreen(
                    onFinished = {
                        // Al terminar, volvemos al calendario
                        navController.navigate(Screen.Calendario.route) {
                            popUpTo(Screen.Calendario.route) { inclusive = true }
                        }
                    }
                )
            }

            // 4. MÍOS (Placeholder)
            composable(Screen.Mios.route) {
                PlaceholderScreen("Perfil y Ajustes")
            }
        }
    }
}

// Un componente temporal para las pantallas que aun no construimos
@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Pantalla: $title", style = MaterialTheme.typography.headlineMedium)
    }
}