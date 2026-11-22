package com.pe.terapia.presentation.home.psicologo.agenda

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
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pe.terapia.domain.model.CitaPacienteDetalle
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaPsicologoScreen(
    onAbrirSala: (String, String, String) -> Unit,
    viewModel: AgendaPsicologoViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var mostrarCalendario by remember { mutableStateOf(false) }
    val formatter = remember { DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "PE")) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("Mi agenda", style = MaterialTheme.typography.headlineSmall) }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.moverDia(-1) }) { Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, "Día anterior") }
                    Text(
                        state.fecha.format(formatter).replaceFirstChar { it.uppercase() },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(onClick = { mostrarCalendario = true }) { Icon(Icons.Outlined.CalendarMonth, "Elegir fecha") }
                    IconButton(onClick = { viewModel.moverDia(1) }) { Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, "Día siguiente") }
                }
            }
        }
        item { OutlinedButton(onClick = { viewModel.seleccionarFecha(java.time.LocalDate.now(ZoneId.of("America/Lima"))) }) { Text("Hoy") } }
        when {
            state.cargando -> item { CircularProgressIndicator() }
            state.error != null -> item { Text(state.error.orEmpty(), color = MaterialTheme.colorScheme.error) }
            state.citas.isEmpty() -> item { Text("No tienes citas para este día.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            else -> items(state.citas, key = { it.citaId }) { cita -> CitaAgendaCard(cita, onAbrirSala) }
        }
    }
    if (mostrarCalendario) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.fecha.atStartOfDay(ZoneId.of("America/Lima")).toInstant().toEpochMilli())
        DatePickerDialog(onDismissRequest = { mostrarCalendario = false }, confirmButton = {
            TextButton(onClick = {
                pickerState.selectedDateMillis?.let { millis ->
                    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                    viewModel.seleccionarFecha(date)
                }
                mostrarCalendario = false
            }) { Text("Elegir") }
        }, dismissButton = { TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") } }) { DatePicker(state = pickerState) }
    }
}

@Composable
private fun CitaAgendaCard(cita: CitaPacienteDetalle, onAbrirSala: (String, String, String) -> Unit) {
    val estadoLabel = when (cita.estado) {
        "PROGRAMADA" -> "Programada"
        "COMPLETADA" -> "Atendida"
        "NO_ASISTIO" -> "No asistió"
        "CANCELADA" -> "Cancelada"
        else -> cita.estado.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
    }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(cita.hora, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Text(estadoLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(cita.nombrePaciente, style = MaterialTheme.typography.titleMedium)
            if (cita.estado == "PROGRAMADA") Button(onClick = { onAbrirSala(cita.citaId, cita.pacienteId, cita.nombrePaciente) }) { Text("Abrir atención") }
        }
    }
}
