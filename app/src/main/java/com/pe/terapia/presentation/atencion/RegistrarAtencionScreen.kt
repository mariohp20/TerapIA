package com.pe.terapia.presentation.atencion

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pe.terapia.R
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

    LaunchedEffect(citaId) {
        viewModel.cargarDatosPrevios(citaId)
    }

    LaunchedEffect(uiState.exito) {
        if (uiState.exito) {
            Toast.makeText(contexto, "Atención registrada y vinculada a la historia clínica", Toast.LENGTH_SHORT).show()
            onGuardadoExitoso()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.registrar_atencion)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.volver))
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.cargandoInicial) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
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
                value = uiState.motivoConsulta,
                onValueChange = viewModel::onMotivoConsultaChange,
                label = { Text(stringResource(R.string.motivo_de_consulta)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.observaciones,
                onValueChange = viewModel::onObservacionesChange,
                label = { Text(stringResource(R.string.observaciones_generales)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.evaluacion,
                onValueChange = viewModel::onEvaluacionChange,
                label = { Text(stringResource(R.string.evaluacion_psicologica)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.evolucion,
                onValueChange = viewModel::onEvolucionChange,
                label = { Text(stringResource(R.string.evolucion_del_paciente)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.indicaciones,
                onValueChange = viewModel::onIndicacionesChange,
                label = { Text(stringResource(R.string.indicaciones_tareas)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.guardarAtencion(citaId, pacienteId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !uiState.guardando
            ) {
                if (uiState.guardando) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text(stringResource(R.string.guardar_atencion_clinica))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}