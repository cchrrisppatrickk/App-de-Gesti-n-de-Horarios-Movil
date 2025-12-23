package com.example.app_de_gestion_de_horarios_movil.ui.features.calendar.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthYearPickerDialog(
    visible: Boolean,
    currentMonth: Int, // 1 a 12
    currentYear: Int,
    onDismiss: () -> Unit,
    onConfirm: (month: Int, year: Int) -> Unit
) {
    if (!visible) return

    // Estados temporales dentro del diálogo
    var tempMonth by remember { mutableIntStateOf(currentMonth) }
    var tempYear by remember { mutableIntStateOf(currentYear) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface, // Surface de Material 3
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Seleccionar fecha",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // --- LISTA DE MESES ---
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .height(150.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f), RoundedCornerShape(8.dp)),
                        state = rememberLazyListState(initialFirstVisibleItemIndex = (tempMonth - 1).coerceAtLeast(0))
                    ) {
                        items(java.time.Month.values()) { month ->
                            val isSelected = month.value == tempMonth
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .clickable { tempMonth = month.value },
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = month.getDisplayName(TextStyle.FULL, Locale.getDefault()).replaceFirstChar { it.titlecase() },
                                    modifier = Modifier.padding(start = 16.dp),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // --- SELECTOR AÑO ---
                    Column(
                        modifier = Modifier.weight(0.6f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(onClick = { tempYear++ }) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Next Year")
                        }
                        Text(
                            text = tempYear.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { tempYear-- }) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Prev Year")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- BOTONES ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onConfirm(tempMonth, tempYear) }) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}