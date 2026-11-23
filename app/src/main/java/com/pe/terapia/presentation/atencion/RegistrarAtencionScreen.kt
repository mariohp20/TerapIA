package com.pe.terapia.presentation.atencion

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Stop
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
    val reconocedor = remember(contexto) { ReconocedorVoz(contexto) }
    var campoActivo by remember { mutableStateOf("motivo") }
    var escuchando by remember { mutableStateOf(false) }
    var procesandoDictado by remember { mutableStateOf(false) }
    var dictadoDetenidoManualmente by remember { mutableStateOf(false) }
    val permisoMicrofono = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) {
            escuchando = true
            procesandoDictado = false
            reconocedor.iniciar(
                alResultado = { viewModel.agregarTranscripcion(campoActivo, it) },
                alParcial = viewModel::actualizarTranscripcionParcial,
                alError = { viewModel.mostrarErrorDictado(it) },
                alFin = { escuchando = false; procesandoDictado = false; viewModel.finalizarDictado() }
            )
        } else viewModel.mostrarErrorDictado("Se necesita permiso para usar el micrófono.")
    }
    val iniciarDictado: (String) -> Unit = { campo ->
        campoActivo = campo
        viewModel.limpiarErrorDictado()
        if (ContextCompat.checkSelfPermission(contexto, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            escuchando = true
            procesandoDictado = false
            reconocedor.iniciar(
                alResultado = { viewModel.agregarTranscripcion(campo, it) },
                alParcial = viewModel::actualizarTranscripcionParcial,
                alError = { mensaje ->
                    if (!dictadoDetenidoManualmente || mensaje != "No se reconoció voz. Puedes intentarlo otra vez.") {
                        viewModel.mostrarErrorDictado(mensaje)
                    }
                },
                alFin = { escuchando = false; procesandoDictado = false; dictadoDetenidoManualmente = false; viewModel.finalizarDictado() }
            )
        } else permisoMicrofono.launch(Manifest.permission.RECORD_AUDIO)
    }
    val detenerDictado: () -> Unit = {
        dictadoDetenidoManualmente = true
        procesandoDictado = true
        reconocedor.detener()
    }

    DisposableEffect(reconocedor) { onDispose { reconocedor.detener() } }

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
            if (!uiState.puedeGuardar) {
                Text("Puedes preparar y dictar las notas, pero solo podrás guardarlas desde la hora programada de la cita.", color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))

            uiState.errorDictado?.let { mensaje ->
                Text(mensaje, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            if (escuchando) {
                Text(
                    when {
                        procesandoDictado -> "Procesando el dictado…"
                        !uiState.transcripcionParcial.isNullOrBlank() -> uiState.transcripcionParcial!!
                        else -> "Escuchando… habla y luego toca detener para insertar el texto."
                    },
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = uiState.motivoConsulta,
                onValueChange = viewModel::onMotivoConsultaChange,
                label = { Text(stringResource(R.string.motivo_de_consulta)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                trailingIcon = {
                    AccionMicrofono(escuchando && campoActivo == "motivo", procesandoDictado, !uiState.guardando,
                        { iniciarDictado("motivo") }, { detenerDictado() })
                }
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.observaciones,
                onValueChange = viewModel::onObservacionesChange,
                label = { Text(stringResource(R.string.observaciones_generales)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                trailingIcon = {
                    AccionMicrofono(escuchando && campoActivo == "observaciones", procesandoDictado, !uiState.guardando,
                        { iniciarDictado("observaciones") }, { detenerDictado() })
                }
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.evaluacion,
                onValueChange = viewModel::onEvaluacionChange,
                label = { Text(stringResource(R.string.evaluacion_psicologica)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                trailingIcon = {
                    AccionMicrofono(escuchando && campoActivo == "evaluacion", procesandoDictado, !uiState.guardando,
                        { iniciarDictado("evaluacion") }, { detenerDictado() })
                }
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.evolucion,
                onValueChange = viewModel::onEvolucionChange,
                label = { Text(stringResource(R.string.evolucion_del_paciente)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                trailingIcon = {
                    AccionMicrofono(escuchando && campoActivo == "evolucion", procesandoDictado, !uiState.guardando,
                        { iniciarDictado("evolucion") }, { detenerDictado() })
                }
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.indicaciones,
                onValueChange = viewModel::onIndicacionesChange,
                label = { Text(stringResource(R.string.indicaciones_tareas)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                trailingIcon = {
                    AccionMicrofono(escuchando && campoActivo == "indicaciones", procesandoDictado, !uiState.guardando,
                        { iniciarDictado("indicaciones") }, { detenerDictado() })
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.guardarAtencion(citaId, pacienteId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !uiState.guardando && uiState.puedeGuardar
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

@Composable
private fun AccionMicrofono(escuchando: Boolean, procesando: Boolean, habilitado: Boolean, onIniciar: () -> Unit, onDetener: () -> Unit) {
    if (procesando) {
        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
    } else if (escuchando) {
        IconButton(onClick = onDetener) { Icon(Icons.Outlined.Stop, contentDescription = "Finalizar y transcribir") }
    } else {
        IconButton(onClick = onIniciar, enabled = habilitado) {
            Icon(Icons.Outlined.Mic, contentDescription = "Dictar en este campo")
        }
    }
}
