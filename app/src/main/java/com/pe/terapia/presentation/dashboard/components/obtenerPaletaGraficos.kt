package com.pe.terapia.presentation.dashboard.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun obtenerPaletaGraficos(): List<Color> {
    return listOf(
        MaterialTheme.colorScheme.primary,          // Turquesa
        MaterialTheme.colorScheme.tertiary,         // Coral
        MaterialTheme.colorScheme.secondary,        // Lavanda
        MaterialTheme.colorScheme.error,            // Rojo
        MaterialTheme.colorScheme.primaryContainer  // Verde oscuro/claro
    )
}