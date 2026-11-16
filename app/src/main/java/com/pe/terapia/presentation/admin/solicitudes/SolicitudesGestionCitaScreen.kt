package com.pe.terapia.presentation.admin.solicitudes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SolicitudesGestionCitaScreen(onVolver: () -> Unit, viewModel: SolicitudesGestionCitaViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.cargar() }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Solicitudes de citas") },
            navigationIcon = { IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver") } },
            actions = { IconButton(onClick = { viewModel.cargar() }) { Icon(Icons.Outlined.Refresh, contentDescription = "Actualizar") } }
        )
    }) { padding ->
        when {
            state.cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Column(Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(state.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = { viewModel.cargar() }) { Text("Reintentar") }
            }
            state.solicitudes.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text("No hay solicitudes pendientes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.aviso?.let { item { Text(it, color = MaterialTheme.colorScheme.primary) } }
                state.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
                items(state.solicitudes, key = { it.id }) { solicitud ->
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(if (solicitud.tipo == "CANCELACION") "Solicitud de cancelación" else "Solicitud de reprogramación", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${solicitud.pacienteNombre} · ${solicitud.fecha} · ${solicitud.hora}")
                            if (solicitud.pagada && solicitud.tipo == "CANCELACION") Text("Hay un pago registrado. La clínica debe revisar si corresponde una devolución según su política; no se procesa automáticamente.", color = MaterialTheme.colorScheme.error)
                            if (solicitud.tipo == "CANCELACION") {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { viewModel.resolver(solicitud.id, "ACEPTAR_CANCELACION") }, enabled = state.procesandoId == null) {
                                        if (state.procesandoId == solicitud.id) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Aceptar y cancelar")
                                    }
                                    OutlinedButton(onClick = { viewModel.resolver(solicitud.id, "RECHAZAR") }, enabled = state.procesandoId == null) { Text("Rechazar") }
                                }
                            } else {
                                Text("Horario actual: ${solicitud.fecha} · ${solicitud.hora}\nPropuesta: ${solicitud.fechaSolicitada ?: "Sin fecha"} · ${solicitud.horaSolicitada ?: "Sin hora"}\nAl aceptar se vuelve a comprobar la disponibilidad y se mueve la misma cita. Si el turno ya fue ocupado, no se aplicará el cambio.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.resolver(solicitud.id, "ACEPTAR_REPROGRAMACION") },
                                        enabled = state.procesandoId == null && solicitud.fechaSolicitada != null && solicitud.horaSolicitada != null
                                    ) { Text("Aceptar y mover cita") }
                                    OutlinedButton(onClick = { viewModel.resolver(solicitud.id, "RECHAZAR") }, enabled = state.procesandoId == null) { Text("Rechazar") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
