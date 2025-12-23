package com.example.app_de_gestion_de_horarios_movil.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import com.example.app_de_gestion_de_horarios_movil.ui.features.calendar.CalendarScreen
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
        // IMPORTANTE: contentWindowInsets(0.dp) permite que el Scaffold
        // no calcule espacio para la status bar automáticamente.
        contentWindowInsets = WindowInsets(0.dp),

        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(120.dp), // Ajustado altura estándar (era 120dp muy alto)
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
        // innerPadding contiene: Top (StatusBar) + Bottom (NavBar)

        NavHost(
            navController = navController,
            startDestination = Screen.Calendario.route,
            // MODIFICACIÓN CRÍTICA: NO aplicamos padding globalmente aquí.
            // modifier = Modifier.padding(innerPadding) <--- ELIMINADO
            modifier = Modifier
        ) {
            // 1. CALENDARIO (EDGE-TO-EDGE SUPERIOR)
            composable(Screen.Calendario.route) {
                // Aquí aplicamos SOLO el padding inferior para no tapar la barra de navegación
                // Pero dejamos el padding superior en 0 para que dibuje detrás de la batería.
                Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
                    CalendarScreen(onNavigateBack = { })
                }
            }

            // 2. TAREAS (STANDARD)
            composable(Screen.Tareas.route) {
                // Para las demás pantallas, sí queremos respetar la barra de estado superior
                Box(modifier = Modifier.padding(innerPadding)) {
                    HomeScreen(
                        onNavigateToWizard = { navController.navigate(Screen.Horarios.route) }
                    )
                }
            }

            // 3. HORARIOS (STANDARD)
            composable(Screen.Horarios.route) {
                Box(modifier = Modifier.padding(innerPadding)) {
                    WizardScreen(
                        onFinished = {
                            navController.navigate(Screen.Calendario.route) {
                                popUpTo(Screen.Calendario.route) { inclusive = true }
                            }
                        }
                    )
                }
            }

            // 4. AJUSTES (STANDARD)
            composable(Screen.Ajustes.route) {
                Box(modifier = Modifier.padding(innerPadding)) {
                    SettingsScreen(
                        onNavigateToTheme = { navController.navigate(Screen.AjustesTema.route) },
                        onNavigateToNotifications = { navController.navigate(Screen.AjustesNotificaciones.route) }
                    )
                }
            }

            // Sub-pantallas
            composable(Screen.AjustesTema.route) {
                Box(modifier = Modifier.padding(innerPadding)) {
                    ThemeSettingsScreen(onBack = { navController.popBackStack() })
                }
            }
            composable(Screen.AjustesNotificaciones.route) {
                Box(modifier = Modifier.padding(innerPadding)) {
                    NotificationsSettingsScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}