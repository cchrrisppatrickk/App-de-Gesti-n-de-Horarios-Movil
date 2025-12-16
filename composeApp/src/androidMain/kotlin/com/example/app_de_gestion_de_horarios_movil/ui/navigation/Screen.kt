package com.example.app_de_gestion_de_horarios.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Wizard : Screen("wizard")
}