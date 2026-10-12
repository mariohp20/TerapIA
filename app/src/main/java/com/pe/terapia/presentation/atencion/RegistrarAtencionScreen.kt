package com.pe.terapia.presentation.atencion

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarAtencionScreen(
    citaId: String,
    pacienteId: String,
    onVolver: () -> Unit,
    onGuardadoExitoso: () -> Unit,
    viewModel: RegistrarAtencionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    val motivo by viewModel.motivoConsulta.collectAsStateWithLifecycle()
    val observaciones by viewModel.observaciones.collectAsStateWithLifecycle()
    val evaluacion by viewModel.evaluacion.collectAsStateWithLifecycle()
    val evolucion by viewModel.evolucion.collectAsStateWithLifecycle()
    val indicaciones by viewModel.indicaciones.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.exito) {
        if (uiState.exito) {
            Toast.makeText(contexto, "Atención registrada y vinculada a la historia clínica", Toast.LENGTH_SHORT).show()
            onGuardadoExitoso()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Atención") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = motivo,
                onValueChange = { viewModel.motivoConsulta.value = it },
                label = { Text("Motivo de Consulta") },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = observaciones,
                onValueChange = { viewModel.observaciones.value = it },
                label = { Text("Observaciones Generales") },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = evaluacion,
                onValueChange = { viewModel.evaluacion.value = it },
                label = { Text("Evaluación Psicológica") },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = evolucion,
                onValueChange = { viewModel.evolucion.value = it },
                label = { Text("Evolución del Paciente") },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = indicaciones,
                onValueChange = { viewModel.indicaciones.value = it },
                label = { Text("Indicaciones / Tareas") },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.guardarAtencion(citaId, pacienteId) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !uiState.guardando
            ) {
                if (uiState.guardando) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar Atención Clínica")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}