package com.pe.terapia.presentation.home.paciente.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pe.terapia.R
import com.pe.terapia.presentation.home.paciente.inicio.components.AccionesRapidas
import com.pe.terapia.presentation.home.paciente.inicio.components.BienvenidaHeader
import com.pe.terapia.presentation.home.paciente.inicio.components.HeroReservaCard
import com.pe.terapia.presentation.home.paciente.inicio.components.ProximaCitaCard
import com.pe.terapia.presentation.home.paciente.inicio.components.SinProximaCitaCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomePacienteScreen(
    onReservarCita: () -> Unit,
    onVerHistorialPagos: () -> Unit,
    onCerrarSesion: () -> Unit,
    onVerHistorial: (String, String) -> Unit,
    onVerTodas: () -> Unit,
    onVerDetalle: (String) -> Unit,
    mensajeError: String? = null,
    viewModel: HomePacienteViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomePacienteContent(
        uiState = uiState,
        mensajeError = mensajeError,
        onReservarCita = onReservarCita,
        onVerHistorialPagos = onVerHistorialPagos,
        onCerrarSesion = onCerrarSesion,
        onVerHistorial = { viewModel.verHistorial(onVerHistorial) },
        onVerTodas = onVerTodas,
        onVerDetalle = onVerDetalle
    )
}

@Composable
private fun HomePacienteContent(
    uiState: HomePacienteUiState,
    mensajeError: String?,
    onReservarCita: () -> Unit,
    onVerHistorialPagos: () -> Unit,
    onCerrarSesion: () -> Unit,
    onVerHistorial: () -> Unit,
    onVerTodas: () -> Unit,
    onVerDetalle: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        BienvenidaHeader(nombre = uiState.nombrePaciente, onCerrarSesion = onCerrarSesion)

        (mensajeError ?: uiState.error)?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }

        HeroReservaCard(onReservar = onReservarCita)

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.inicio_accesos_titulo), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            AccionesRapidas(
                onReservar = onReservarCita,
                onPagos = onVerHistorialPagos,
                onHistorial = onVerHistorial
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.home_proxima_cita), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "Ver todas \u2192",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onVerTodas() }
                )
            }
            when {
                uiState.cargando -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                uiState.proximaCita != null -> ProximaCitaCard(
                    cita = uiState.proximaCita,
                    onClick = { onVerDetalle(uiState.proximaCita.id) }
                )
                else -> SinProximaCitaCard()
            }
        }
    }
}