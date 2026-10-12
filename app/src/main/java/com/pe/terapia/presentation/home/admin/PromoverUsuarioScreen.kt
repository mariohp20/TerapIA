package com.pe.terapia.presentation.home.admin

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pe.terapia.domain.model.Usuario
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

    // Variables de estado locales para los formularios
    var pacienteSeleccionado by remember { mutableStateOf<Usuario?>(null) }
    var descripcion by remember { mutableStateOf("") }
    var especialidadesSeleccionadas by remember { mutableStateOf(setOf<String>()) }
    var sedesSeleccionadas by remember { mutableStateOf(setOf<String>()) }
    var expandedPacientes by remember { mutableStateOf(false) }

    // Reaccionar al estado de éxito
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

            // 1. Selector de Paciente
            Text("Seleccionar Paciente", style = MaterialTheme.typography.titleMedium)
            ExposedDropdownMenuBox(
                expanded = expandedPacientes,
                onExpandedChange = { expandedPacientes = !expandedPacientes },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = pacienteSeleccionado?.let { "${it.nombres} ${it.apellidos}" } ?: "Elija un paciente",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPacientes) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedPacientes,
                    onDismissRequest = { expandedPacientes = false }
                ) {
                    if (uiState.pacientes.isEmpty()) {
                        DropdownMenuItem(text = { Text("No hay pacientes disponibles") }, onClick = {})
                    } else {
                        uiState.pacientes.forEach { paciente ->
                            DropdownMenuItem(
                                text = { Text("${paciente.nombres} ${paciente.apellidos} - ${paciente.dni ?: "Sin DNI"}") },
                                onClick = {
                                    pacienteSeleccionado = paciente
                                    expandedPacientes = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Descripción Profesional
            Text("Descripción Profesional", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(100.dp),
                placeholder = { Text("Ej. Especialista con 10 años de experiencia...") },
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Especialidades
            Text("Especialidades", style = MaterialTheme.typography.titleMedium)
            uiState.especialidades.forEach { especialidad ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = especialidadesSeleccionadas.contains(especialidad.id),
                        onCheckedChange = { checked ->
                            val nuevas = especialidadesSeleccionadas.toMutableSet()
                            if (checked) nuevas.add(especialidad.id) else nuevas.remove(especialidad.id)
                            especialidadesSeleccionadas = nuevas
                        }
                    )
                    Text(text = especialidad.nombre)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Sedes
            Text("Sedes de Atención", style = MaterialTheme.typography.titleMedium)
            uiState.sedes.forEach { sede ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = sedesSeleccionadas.contains(sede.id),
                        onCheckedChange = { checked ->
                            val nuevas = sedesSeleccionadas.toMutableSet()
                            if (checked) nuevas.add(sede.id) else nuevas.remove(sede.id)
                            sedesSeleccionadas = nuevas
                        }
                    )
                    Text(text = sede.nombre)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón Guardar
            Button(
                onClick = {
                    if (pacienteSeleccionado != null) {
                        viewModel.promover(
                            usuario = pacienteSeleccionado!!,
                            descripcionProfesional = descripcion,
                            especialidadIds = especialidadesSeleccionadas.toList(),
                            sedeIds = sedesSeleccionadas.toList()
                        )
                    } else {
                        Toast.makeText(context, "Debe seleccionar un paciente", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !uiState.guardando && pacienteSeleccionado != null
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