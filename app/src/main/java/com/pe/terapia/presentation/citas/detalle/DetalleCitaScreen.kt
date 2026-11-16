package com.pe.terapia.presentation.citas.detalle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pe.terapia.domain.model.EstadoPago
import com.pe.terapia.domain.model.ProximaCita
import com.pe.terapia.presentation.home.paciente.inicio.components.EstadoCitaChip
import com.pe.terapia.presentation.home.paciente.inicio.components.CitaQrDialog
import com.pe.terapia.domain.model.EstadoCita
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleCitaScreen(
    citaId: String,
    onVolver: () -> Unit,
    onVerPagos: () -> Unit,
    onVerHistorial: (String, String, String) -> Unit,
    viewModel: DetalleCitaViewModel = koinViewModel(parameters = { parametersOf(citaId) })
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de la cita", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding)) {
            when (val state = uiState) {
                DetalleCitaUiState.Cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is DetalleCitaUiState.Error -> Text(
                    state.mensaje, color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )
                is DetalleCitaUiState.Contenido -> DetalleCitaContenido(
                    cita = state.cita,
                    pacienteId = state.pacienteId,
                    pacienteNombre = state.pacienteNombre,
                    onVerPagos = onVerPagos,
                    onVerHistorial = onVerHistorial
                )
            }
        }
    }
}

@Composable
private fun DetalleCitaContenido(
    cita: ProximaCita,
    pacienteId: String,
    pacienteNombre: String,
    onVerPagos: () -> Unit,
    onVerHistorial: (String, String, String) -> Unit
) {
    var mostrarQr by remember(cita.id) { mutableStateOf(false) }
    val iniciales = cita.psicologoNombre.split(" ").filter(String::isNotBlank).take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TARJETA DEL PROFESIONAL
        Card(
            Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(60.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Text(iniciales, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(cita.psicologoNombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(cita.especialidadNombre, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
                EstadoCitaChip(cita) // Reutilizamos el chip que ya creamos
            }
        }

        // DESCRIPCIÓN
        SeccionCard("Sobre el profesional") {
            Text(cita.psicologoDescripcion.ifBlank { "El profesional aún no tiene una descripción detallada en su perfil." }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // FECHA Y LUGAR
        SeccionCard("Detalles de la atención") {
            InfoFila(Icons.Outlined.CalendarMonth, cita.fecha)
            InfoFila(Icons.Outlined.Schedule, cita.hora)
            InfoFila(Icons.Outlined.LocationOn, listOf(cita.sedeNombre, cita.sedeDireccion).filter(String::isNotBlank).joinToString(" · "))
        }

        // PAGOS
        SeccionCard("Estado del Pago") {
            Text(
                when (cita.estadoPago) {
                    EstadoPago.PAGADO.name -> "El pago ha sido confirmado."
                    EstadoPago.PENDIENTE.name -> "El pago está pendiente."
                    else -> cita.estadoPago
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            OutlinedButton(onClick = onVerPagos, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("Ver mis pagos y comprobantes")
            }
        }

        SeccionCard("Código QR de la cita") {
            Text(
                text = when (cita.estado) {
                    EstadoCita.COMPLETADA.name -> "Esta cita figura como atendida. Puedes mostrar el QR para consultar su registro."
                    EstadoCita.CANCELADA.name -> "Esta cita fue cancelada. El QR solo permite consultar su estado; no registra asistencia."
                    EstadoCita.NO_ASISTIO.name -> "Esta cita figura como inasistencia. El QR solo permite consultar su estado."
                    else -> "Muestra este código al psicólogo para validar tu cita al llegar."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(onClick = { mostrarQr = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.QrCode2, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Ver QR de esta cita")
            }
            if (cita.estado == EstadoCita.COMPLETADA.name) {
                TextButton(
                    onClick = { onVerHistorial(pacienteId, pacienteNombre, cita.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver mi historial clínico")
                }
            }
        }
    }

    if (mostrarQr) {
        CitaQrDialog(
            token = cita.id,
            onDismiss = { mostrarQr = false },
            mensaje = if (cita.estado == EstadoCita.PROGRAMADA.name) {
                "Muestra este código al psicólogo para validar la cita."
            } else {
                "Este código consulta la cita y su estado. No cambia ni registra la asistencia."
            }
        )
    }
}

@Composable
private fun SeccionCard(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            contenido()
        }
    }
}

@Composable
private fun InfoFila(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
