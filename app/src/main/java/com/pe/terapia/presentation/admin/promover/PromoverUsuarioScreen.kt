package com.pe.terapia.presentation.admin.promover

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromoverUsuarioScreen(
    onVolver: () -> Unit,
    onPromocionExitosa: () -> Unit,
    viewModel: PromoverUsuarioViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var expandedPacientes by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.promocionExitosa) {
        if (uiState.promocionExitosa) {
            Toast.makeText(context, "Usuario promovido exitosamente", Toast.LENGTH_SHORT).show()
            onPromocionExitosa()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Promover a Psicólogo") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.cargandoInicial) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            uiState.error?.let { errorMsg ->
                Text(text = errorMsg, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Paciente con filtro
            Text("Seleccionar Paciente", style = MaterialTheme.typography.titleMedium)
            ExposedDropdownMenuBox(
                expanded = expandedPacientes,
                onExpandedChange = { expandedPacientes = !expandedPacientes },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.queryBusqueda,
                    onValueChange = {
                        viewModel.onQueryBusquedaChange(it)
                        expandedPacientes = true
                    },
                    placeholder = { Text("Buscar por nombre o DNI...") },
                    singleLine = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPacientes) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                DropdownMenu(
                    expanded = expandedPacientes,
                    onDismissRequest = { expandedPacientes = false },
                    properties = PopupProperties(focusable = false),
                    modifier = Modifier.exposedDropdownSize()
                ) {
                    val filtrados = uiState.pacientesFiltrados
                    if (filtrados.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No se encontraron pacientes") },
                            onClick = { expandedPacientes = false }
                        )
                    } else {
                        filtrados.forEach { paciente ->
                            DropdownMenuItem(
                                text = { Text("${paciente.nombres} ${paciente.apellidos} - ${paciente.dni ?: "Sin DNI"}") },
                                onClick = {
                                    viewModel.seleccionarPaciente(paciente)
                                    expandedPacientes = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Descripción Profesional
            Text("Descripción Profesional", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = uiState.formDescripcion,
                onValueChange = viewModel::onDescripcionChange,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(100.dp),
                placeholder = { Text("Ej. Especialista con 10 años de experiencia...") },
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Especialidades
            Text("Especialidades", style = MaterialTheme.typography.titleMedium)
            uiState.especialidades.forEach { especialidad ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = uiState.formEspecialidades.contains(especialidad.id),
                        onCheckedChange = { viewModel.toggleEspecialidad(especialidad.id) }
                    )
                    Text(text = especialidad.nombre)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sedes
            Text("Sedes de Atención", style = MaterialTheme.typography.titleMedium)
            uiState.sedes.forEach { sede ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = uiState.formSedes.contains(sede.id),
                        onCheckedChange = { viewModel.toggleSede(sede.id) }
                    )
                    Text(text = sede.nombre)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Guardar
            Button(
                onClick = viewModel::promover,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !uiState.guardando && uiState.pacienteSeleccionado != null
            ) {
                if (uiState.guardando) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Confirmar Promoción")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}