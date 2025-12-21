package com.example.app_de_gestion_de_horarios_movil.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.app_de_gestion_de_horarios_movil.ui.features.calendar.CalendarScreen // <--- IMPORTANTE
import com.example.app_de_gestion_de_horarios_movil.ui.features.home.HomeScreen
import com.example.app_de_gestion_de_horarios_movil.ui.features.settings.SettingsScreen
import com.example.app_de_gestion_de_horarios_movil.ui.features.settings.subscreens.NotificationsSettingsScreen
import com.example.app_de_gestion_de_horarios_movil.ui.features.settings.subscreens.ThemeSettingsScreen
import com.example.app_de_gestion_de_horarios_movil.ui.features.wizard.WizardScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        Screen.Calendario,
        Screen.Tareas,
        Screen.Horarios,
        Screen.Ajustes
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(120.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                    NavigationBarItem(
                        icon = {
                            screen.icon?.let { iconVector ->
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Calendario.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. CALENDARIO (CONECTADO)
            composable(Screen.Calendario.route) {
                CalendarScreen(
                    // CalendarViewModel se inyecta automáticamente con Koin dentro de la pantalla
                    // onNavigateBack no es necesario en una pestaña principal, pero lo dejamos vacío por si acaso
                    onNavigateBack = { }
                )
            }

            // 2. TAREAS
            composable(Screen.Tareas.route) {
                HomeScreen(
                    onNavigateToWizard = { navController.navigate(Screen.Horarios.route) }
                )
            }

            // 3. HORARIOS
            composable(Screen.Horarios.route) {
                WizardScreen(
                    onFinished = {
                        navController.navigate(Screen.Calendario.route) {
                            popUpTo(Screen.Calendario.route) { inclusive = true }
                        }
                    }
                )
            }

            // 4. AJUSTES
            composable(Screen.Ajustes.route) {
                SettingsScreen(
                    onNavigateToTheme = { navController.navigate(Screen.AjustesTema.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.AjustesNotificaciones.route) }
                )
            }

            // Sub-pantallas
            composable(Screen.AjustesTema.route) {
                ThemeSettingsScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.AjustesNotificaciones.route) {
                NotificationsSettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

// PlaceholderScreen se mantiene por si lo usas en el futuro para secciones no terminadas
@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Pantalla: $title",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}