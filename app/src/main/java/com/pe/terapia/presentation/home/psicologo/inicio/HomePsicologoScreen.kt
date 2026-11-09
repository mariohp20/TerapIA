package com.pe.terapia.presentation.home.psicologo.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.pe.terapia.presentation.home.psicologo.inicio.HomePsicologoViewModel
import com.pe.terapia.presentation.home.psicologo.inicio.components.PsicologoAccionesRapidas
import com.pe.terapia.presentation.home.psicologo.inicio.components.PsicologoHeader
import com.pe.terapia.presentation.home.psicologo.inicio.components.PsicologoMetricaCard
import com.pe.terapia.presentation.home.psicologo.inicio.components.PsicologoProximaCitaCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomePsicologoScreen(
    onGestionarPagos: () -> Unit,
    onCerrarSesion: () -> Unit,
    onAbrirSalaConsulta: (citaId: String, pacienteId: String, pacienteNombre: String) -> Unit,
    onIrAHistorialClinico: () -> Unit,
    onDashboardPsicologo: () -> Unit,
    viewModel: HomePsicologoViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.cargarDatosHoy()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {

        PsicologoHeader(
            nombrePsicologo = uiState.nombrePsicologo,
            onCerrarSesion = onCerrarSesion
        )

        if (uiState.cargando) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PsicologoMetricaCard(modifier = Modifier.weight(1f), icon = Icons.Outlined.Event, titulo = "Citas\nde hoy", valor = uiState.citasHoy.toString())
                PsicologoMetricaCard(modifier = Modifier.weight(1f), icon = Icons.Outlined.PendingActions, titulo = "Pendientes", valor = uiState.pendientes.toString())
                PsicologoMetricaCard(modifier = Modifier.weight(1f), icon = Icons.Outlined.PeopleAlt, titulo = "Pacientes\nactivos", valor = uiState.pacientesActivos.toString())
            }

            if (uiState.citasPendientesResultado.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Citas pendientes de registrar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    uiState.citasPendientesResultado.forEach { cita ->
                        Card(
                            onClick = { onAbrirSalaConsulta(cita.citaId, cita.pacienteId, cita.nombrePaciente) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.42f))
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(cita.nombrePaciente, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text("${cita.fecha} · ${cita.hora}", style = MaterialTheme.typography.bodyMedium)
                                Text("Registra la atención o marca que no asistió", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            if (uiState.proximasCitas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(20.dp)).padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tienes citas programadas para hoy.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val proxima = uiState.proximasCitas.first()
                PsicologoProximaCitaCard(
                    cita = proxima,
                    onClick = { onAbrirSalaConsulta(proxima.citaId, proxima.pacienteId, proxima.nombrePaciente) }
                )
            }

            PsicologoAccionesRapidas(
                onDashboardPsicologo = onDashboardPsicologo,
                onIrAHistorialClinico = onIrAHistorialClinico,
                onGestionarPagos = onGestionarPagos
            )
        }
    }
}
