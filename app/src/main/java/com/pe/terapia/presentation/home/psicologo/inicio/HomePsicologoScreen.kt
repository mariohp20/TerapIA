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
    onRegistrarAtencion: (citaId: String, pacienteId: String) -> Unit,
    onIrAHistorialClinico: () -> Unit,
    viewModel: HomePsicologoViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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

            if (uiState.proximasCitas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(20.dp)).padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tienes citas programadas para hoy.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                PsicologoProximaCitaCard(
                    cita = uiState.proximasCitas.first(),
                    onClick = { onRegistrarAtencion(uiState.proximasCitas.first().citaId, uiState.proximasCitas.first().pacienteId) }
                )
            }

            PsicologoAccionesRapidas(
                onIrAHistorialClinico = onIrAHistorialClinico,
                onGestionarPagos = onGestionarPagos
            )
        }
    }
}