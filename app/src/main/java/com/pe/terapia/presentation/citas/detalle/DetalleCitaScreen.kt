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
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pe.terapia.domain.model.EstadoPago
import com.pe.terapia.domain.model.ProximaCita
import com.pe.terapia.presentation.home.paciente.inicio.components.EstadoCitaChip
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleCitaScreen(
    citaId: String,
    onVolver: () -> Unit,
    onVerPagos: () -> Unit,
    viewModel: DetalleCitaViewModel = koinViewModel(parameters = { parametersOf(citaId) })
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cita = uiState.cita

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
            when {
                uiState.cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                cita == null -> Text(
                    uiState.error ?: "No se encontró la cita.", color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )
                else -> DetalleCitaContenido(cita, onVerPagos)
            }
        }
    }
}

@Composable
private fun DetalleCitaContenido(cita: ProximaCita, onVerPagos: () -> Unit) {
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