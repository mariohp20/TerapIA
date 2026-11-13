package com.pe.terapia.presentation.admin.gestionar

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionarPsicologoScreen(
    onVolver: () -> Unit,
    viewModel: GestionarPsicologoViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.exito) {
        if (uiState.exito) {
            Toast.makeText(context, "Psicólogo actualizado", Toast.LENGTH_SHORT).show()
            viewModel.seleccionarPsicologo(null) // Volver al directorio
        }
    }

    // Interceptar el botón de "atrás" del sistema Android si estamos editando
    BackHandler(enabled = uiState.psicologoSeleccionado != null) {
        viewModel.seleccionarPsicologo(null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.psicologoSeleccionado == null) "Directorio" else "Editar Perfil") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.psicologoSeleccionado != null) viewModel.seleccionarPsicologo(null) else onVolver()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.cargandoInicial) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }

        if (uiState.psicologoSeleccionado == null) {
            // VISTA 1: DIRECTORIO DE PSICÓLOGOS
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.psicologos.isEmpty()) {
                    item { Text("No hay psicólogos registrados.") }
                }
                items(uiState.psicologos) { psicologo ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.seleccionarPsicologo(psicologo) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("${psicologo.nombres} ${psicologo.apellidos}", fontWeight = FontWeight.Bold)
                            Text("Sedes asignadas: ${psicologo.sedeIds.size}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        } else {
            // VISTA 2: FORMULARIO DE EDICIÓN (State Hoisting)
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(16.dp))
                uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error); Spacer(Modifier.height(8.dp)) }

                Text("Descripción Profesional", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = uiState.formDescripcion,
                    onValueChange = viewModel::onDescripcionChange,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(100.dp),
                    maxLines = 4
                )

                Spacer(Modifier.height(24.dp))
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

                Spacer(Modifier.height(16.dp))
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

                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { viewModel.guardarCambios() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !uiState.guardando
                ) {
                    if (uiState.guardando) CircularProgressIndicator(Modifier.size(24.dp))
                    else Text("Guardar Cambios")
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}