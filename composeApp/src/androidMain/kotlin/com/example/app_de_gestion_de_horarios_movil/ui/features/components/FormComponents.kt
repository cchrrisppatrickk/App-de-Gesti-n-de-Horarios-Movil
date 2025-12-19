package com.example.app_de_gestion_de_horarios_movil.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ReadOnlyRow(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    // CAMBIO: Estilo "Outlined" para coincidir con los TextField
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    val shape = RoundedCornerShape(8.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            // Quitamos el background sólido y ponemos un borde sutil
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            // El icono usa el color variante (gris claro) para no competir con los inputs activos
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            // El texto en blanco/gris claro para lectura fácil sobre fondo oscuro
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun IconSelectorRow(
    selectedIconId: String,
    onIconSelected: (String) -> Unit
) {
    val iconsList = TaskIcons.availableIcons.entries.toList()

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(iconsList) { (id, iconVector) ->
            val isSelected = selectedIconId == id

            // CAMBIO: Colores personalizados para el tema Matte
            val backgroundColor = if (isSelected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) // Coral muy transparente
            else
                Color.Transparent // Sin fondo si no está seleccionado

            val iconColor = if (isSelected)
                MaterialTheme.colorScheme.primary // Coral vibrante
            else
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) // Gris apagado

            val borderColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

            // Contenedor del Icono
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(50.dp) // Un poco más grande para mejor toque
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .border(
                        width = 1.dp, // Borde fino y elegante
                        color = borderColor,
                        shape = CircleShape
                    )
                    .clickable { onIconSelected(id) }
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ColorSelectorRow(
    selectedColorHex: String,
    onColorSelected: (String) -> Unit
) {
    // Paleta de colores (Puedes añadir más colores pastel si quieres que encajen mejor con el modo oscuro)
    val colors = listOf(
        "#3498DB", // Azul
        "#E74C3C", // Rojo
        "#2ECC71", // Verde
        "#F1C40F", // Amarillo
        "#9B59B6", // Morado
        "#F39C12"  // Naranja
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(colors) { colorHex ->
            val isSelected = selectedColorHex == colorHex

            // Renderizamos el círculo de color
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(colorHex)))
                    .clickable { onColorSelected(colorHex) }
            ) {
                // CAMBIO: Anillo de selección INTERNO
                // Si está seleccionado, mostramos un anillo blanco (OnSurface)
                // para que destaque sobre cualquier color de fondo.
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.surface, // Borde del color del fondo para "recortar"
                                shape = CircleShape
                            )
                    )
                    // Segundo borde blanco exterior (opcional, para mayor contraste)
                    Box(
                        modifier = Modifier
                            .size(34.dp) // Un poco más pequeño
                            .border(
                                width = 2.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                    )
                }
            }
        }
    }
}