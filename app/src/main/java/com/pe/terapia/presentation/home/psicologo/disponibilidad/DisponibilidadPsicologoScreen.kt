package com.pe.terapia.presentation.home.psicologo.disponibilidad

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisponibilidadPsicologoScreen(viewModel: DisponibilidadPsicologoViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var mostrarCalendario by remember { mutableStateOf(false) }
    var mostrarSedes by remember { mutableStateOf(false) }
    val formatter = remember { DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "PE")) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("Disponibilidad", style = MaterialTheme.typography.headlineSmall) }
        item { Text("Consulta los turnos libres de tu horario publicado.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (state.sedes.size > 1) {
            item {
                Column {
                    OutlinedButton(onClick = { mostrarSedes = true }) { Text(state.sedeSeleccionada?.nombre ?: "Elegir sede") }
                    DropdownMenu(expanded = mostrarSedes, onDismissRequest = { mostrarSedes = false }) {
                        state.sedes.forEach { sede ->
                            DropdownMenuItem(text = { Text(sede.nombre) }, onClick = { viewModel.seleccionarSede(sede); mostrarSedes = false })
                        }
                    }
                }
            }
        } else state.sedeSeleccionada?.let { sede ->
            item { Text("Sede: ${sede.nombre}", style = MaterialTheme.typography.titleSmall) }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Fecha", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(state.fecha.format(formatter).replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium)
                    }
                    IconButton(onClick = { mostrarCalendario = true }) { Icon(Icons.Outlined.CalendarMonth, "Elegir fecha") }
                }
            }
        }
        when {
            state.cargando -> item { CircularProgressIndicator() }
            state.error != null -> item { Text(state.error.orEmpty(), color = MaterialTheme.colorScheme.error) }
            state.sedes.isEmpty() -> item { Text("Aún no tienes una sede asignada. Solicita al administrador que configure tu disponibilidad.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            state.horarios.isEmpty() -> item { Text("No hay turnos disponibles para esta sede y fecha. Puede que no tengas horario ese día o que los turnos ya estén ocupados.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            else -> {
                item { Text("Turnos libres · ${state.horarios.size}", style = MaterialTheme.typography.titleMedium) }
                items(state.horarios, key = { it.inicioEpochMillis }) { slot ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))) {
                        Text(slot.hora, Modifier.fillMaxWidth().padding(16.dp), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
        item { Text("El horario semanal y sus excepciones los administra la clínica para proteger las citas ya reservadas.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
    if (mostrarCalendario) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.fecha.atStartOfDay(ZoneId.of("America/Lima")).toInstant().toEpochMilli())
        DatePickerDialog(onDismissRequest = { mostrarCalendario = false }, confirmButton = {
            TextButton(onClick = {
                pickerState.selectedDateMillis?.let { millis -> viewModel.seleccionarFecha(Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()) }
                mostrarCalendario = false
            }) { Text("Elegir") }
        }, dismissButton = { TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") } }) { DatePicker(state = pickerState) }
    }
}
