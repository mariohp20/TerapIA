package com.pe.terapia.presentation.admin.horario

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
import androidx.compose.ui.graphics.Color
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


    var expandedPsicologos by remember { mutableStateOf(false) }
    var expandedDias by remember { mutableStateOf(false) }
    var expandedHoraInicio by remember { mutableStateOf(false) }
    var expandedHoraFin by remember { mutableStateOf(false) }

    val diasSemana = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    val horasDisponibles = (8..22).toList()

    LaunchedEffect(uiState.exito) {
        if (uiState.exito) {
            Toast.makeText(contexto, "Horario generado con éxito", Toast.LENGTH_LONG).show()
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
        if (uiState.cargandoInicial) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

            // Selector de Psicologo
            Text("Psicólogo", style = MaterialTheme.typography.titleMedium)
            ExposedDropdownMenuBox(
                expanded = expandedPsicologos,
                onExpandedChange = { expandedPsicologos = !expandedPsicologos },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.psicologoSeleccionado?.let { "${it.nombres} ${it.apellidos}" } ?: "Elija un psicólogo",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPsicologos) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedPsicologos,
                    onDismissRequest = { expandedPsicologos = false }
                ) {
                    uiState.psicologosActivos.forEach { psicologo ->
                        DropdownMenuItem(
                            text = { Text("${psicologo.nombres} ${psicologo.apellidos}") },
                            onClick = {
                                viewModel.seleccionarPsicologo(psicologo)
                                expandedPsicologos = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Seleccion de Sedes
            Text("Sedes Disponibles", style = MaterialTheme.typography.titleMedium)

            if (uiState.psicologoSeleccionado == null) {
                Text("Seleccione un psicólogo primero", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
            } else {
                val sedesDelPsicologo = uiState.sedesDelPsicologo

                if (sedesDelPsicologo.isEmpty()) {
                    Text("Este psicólogo no tiene sedes asignadas", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                } else {
                    sedesDelPsicologo.forEach { sede ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = uiState.sedesSeleccionadas.contains(sede.id),
                                onCheckedChange = { viewModel.toggleSede(sede.id) }
                            )
                            Text(text = sede.nombre)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Selector de Dia
            Text("Día de la semana", style = MaterialTheme.typography.titleMedium)
            ExposedDropdownMenuBox(
                expanded = expandedDias,
                onExpandedChange = { expandedDias = !expandedDias },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = diasSemana[uiState.diaSeleccionado - 1],
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDias) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expandedDias, onDismissRequest = { expandedDias = false }) {
                    diasSemana.forEachIndexed { index, dia ->
                        DropdownMenuItem(
                            text = { Text(dia) },
                            onClick = {
                                viewModel.seleccionarDia(index + 1)
                                expandedDias = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Selectores de Horas
            Text("Rango de horas (Intervalos de 1 hora)", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                ExposedDropdownMenuBox(
                    expanded = expandedHoraInicio,
                    onExpandedChange = { expandedHoraInicio = !expandedHoraInicio },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = "${uiState.horaInicioSeleccionada}:00",
                        onValueChange = {},
                        label = { Text("Inicio") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHoraInicio) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedHoraInicio, onDismissRequest = { expandedHoraInicio = false }) {
                        horasDisponibles.forEach { hora ->
                            DropdownMenuItem(
                                text = { Text("$hora:00") },
                                onClick = {
                                    viewModel.seleccionarHoraInicio(hora)
                                    expandedHoraInicio = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedHoraFin,
                    onExpandedChange = { expandedHoraFin = !expandedHoraFin },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = "${uiState.horaFinSeleccionada}:00",
                        onValueChange = {},
                        label = { Text("Fin") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHoraFin) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedHoraFin, onDismissRequest = { expandedHoraFin = false }) {
                        horasDisponibles.filter { it > uiState.horaInicioSeleccionada }.forEach { hora ->
                            DropdownMenuItem(
                                text = { Text("$hora:00") },
                                onClick = {
                                    viewModel.seleccionarHoraFin(hora)
                                    expandedHoraFin = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Guardar
            Button(
                onClick = viewModel::guardarHorario,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !uiState.guardando && uiState.psicologoSeleccionado != null && uiState.sedesSeleccionadas.isNotEmpty()
            ) {
                if (uiState.guardando) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Crear Horario")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}