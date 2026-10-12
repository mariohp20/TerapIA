package com.pe.terapia.presentation.admin.horario

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
fun AsignarHorarioScreen(
    onVolver: () -> Unit,
    viewModel: AsignarHorarioViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    val psicologoId by viewModel.psicologoId.collectAsStateWithLifecycle()
    val sedeId by viewModel.sedeId.collectAsStateWithLifecycle()
    val diaSemana by viewModel.diaSemana.collectAsStateWithLifecycle()
    val horaInicio by viewModel.horaInicio.collectAsStateWithLifecycle()
    val horaFin by viewModel.horaFin.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.exito) {
        if (uiState.exito) {
            Toast.makeText(contexto, "Horario generado con éxito en Firebase", Toast.LENGTH_LONG).show()
            onVolver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurar Horario") },
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
                value = psicologoId,
                onValueChange = { viewModel.psicologoId.value = it },
                label = { Text("ID del Psicólogo") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = sedeId,
                onValueChange = { viewModel.sedeId.value = it },
                label = { Text("ID de la Sede") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = diaSemana,
                onValueChange = { viewModel.diaSemana.value = it },
                label = { Text("Día de la semana (1 = Lunes, 7 = Domingo)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = horaInicio,
                    onValueChange = { viewModel.horaInicio.value = it },
                    label = { Text("Inicio (HH:mm)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = horaFin,
                    onValueChange = { viewModel.horaFin.value = it },
                    label = { Text("Fin (HH:mm)") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.guardarHorario() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !uiState.guardando
            ) {
                if (uiState.guardando) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Crear Horario")
                }
            }
        }
    }
}