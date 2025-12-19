package com.example.app_de_gestion_de_horarios_movil.ui.theme

import androidx.compose.ui.graphics.Color

// --- MODO OSCURO (Estilo "Structured" / Matte) ---
// El fondo no es negro absoluto (#000000), es un gris carbón muy profundo.
val BackgroundBlack = Color(0xFF161616)

// Las tarjetas/surfaces son apenas un tono más claro para dar profundidad.
val SurfaceDark = Color(0xFF252525)

// *** EL CAMBIO CLAVE *** // En lugar de rojo oscuro sangre, usamos este "Coral/Salmón" vibrante de la imagen.
val PrimaryRed = Color(0xFFFF746C)

// Un tono un poco más apagado del coral para estados presionados o bordes sutiles.
val SecondaryRed = Color(0xFFE05A50)

// Blanco casi puro para que el texto resalte mucho sobre el fondo mate.
val TextGray = Color(0xFFEEEEEE)

// Gris medio con un toque azulado/frío para subtítulos (como las horas "12:30 PM").
val TextDarkGray = Color(0xFF9E9E9E)

// Barra de herramientas integrada con el fondo.
val ToolbarDark = Color(0xFF161616)


// --- MODO CLARO (Adaptado para mantener la identidad "Coral") ---
val BackgroundWhite = Color(0xFFF9F9F9)       // Un blanco humo, no blanco nuclear
val SurfaceWhite = Color(0xFFFFFFFF)
// En modo claro, el coral necesita ser un pelín más oscuro para leerse bien sobre blanco
val PrimaryRedLight = Color(0xFFFF5F55)
val TextBlack = Color(0xFF1C1C1E)
val TextLightGray = Color(0xFF8E8E93)