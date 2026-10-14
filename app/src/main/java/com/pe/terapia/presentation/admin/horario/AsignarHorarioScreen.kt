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
import com.pe.terapia.domain.model.Psicologo // <-- Importamos Psicologo
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsignarHorarioScreen(
    onVolver: () -> Unit,
    viewModel: AsignarHorarioViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    // Estados Locales para la UI
    var psicologoSeleccionado by remember { mutableStateOf<Psicologo?>(null) } // <-- Cambiado a Psicologo
    var sedesSeleccionadas by remember { mutableStateOf(setOf<String>()) }
    var diaSeleccionado by remember { mutableStateOf(1) } // 1 = Lunes
    var horaInicioSeleccionada by remember { mutableStateOf(8) }
    var horaFinSeleccionada by remember { mutableStateOf(9) }

    // Estados para los menús desplegables
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

            // 1. Selector de Psicólogo
            Text("Psicólogo", style = MaterialTheme.typography.titleMedium)
            ExposedDropdownMenuBox(
                expanded = expandedPsicologos,
                onExpandedChange = { expandedPsicologos = !expandedPsicologos },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = psicologoSeleccionado?.let { "${it.nombres} ${it.apellidos}" } ?: "Elija un psicólogo",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPsicologos) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedPsicologos,
                    onDismissRequest = { expandedPsicologos = false }
                ) {
                    uiState.psicologos.filter { it.activo }.forEach { psicologo -> // Filtramos por activos
                        DropdownMenuItem(
                            text = { Text("${psicologo.nombres} ${psicologo.apellidos}") },
                            onClick = {
                                // Limpiamos las sedes si se cambia de doctor para evitar errores
                                if (psicologoSeleccionado?.id != psicologo.id) {
                                    sedesSeleccionadas = emptySet()
                                }
                                psicologoSeleccionado = psicologo
                                expandedPsicologos = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Selección de Sedes (FILTRADA POR EL PSICÓLOGO)
            Text("Sedes Disponibles", style = MaterialTheme.typography.titleMedium)

            if (psicologoSeleccionado == null) {
                Text("Seleccione un psicólogo primero", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
            } else {
                // Filtramos las sedes generales para mostrar solo aquellas cuyos IDs estén en la lista sedeIds del psicólogo
                val sedesDelPsicologo = uiState.sedes.filter { psicologoSeleccionado!!.sedeIds.contains(it.id) }

                if (sedesDelPsicologo.isEmpty()) {
                    Text("Este psicólogo no tiene sedes asignadas", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                } else {
                    sedesDelPsicologo.forEach { sede ->
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
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Selector de Día de la Semana
            Text("Día de la semana", style = MaterialTheme.typography.titleMedium)
            ExposedDropdownMenuBox(
                expanded = expandedDias,
                onExpandedChange = { expandedDias = !expandedDias },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = diasSemana[diaSeleccionado - 1],
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
                                diaSeleccionado = index + 1
                                expandedDias = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Selectores de Horas (Solo Horas Exactas)
            Text("Rango de horas (Intervalos de 1 hora)", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {

                // Inicio
                ExposedDropdownMenuBox(
                    expanded = expandedHoraInicio,
                    onExpandedChange = { expandedHoraInicio = !expandedHoraInicio },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = "$horaInicioSeleccionada:00",
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
                                    horaInicioSeleccionada = hora
                                    if (horaFinSeleccionada <= hora) horaFinSeleccionada = hora + 1
                                    expandedHoraInicio = false
                                }
                            )
                        }
                    }
                }

                // Fin
                ExposedDropdownMenuBox(
                    expanded = expandedHoraFin,
                    onExpandedChange = { expandedHoraFin = !expandedHoraFin },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = "$horaFinSeleccionada:00",
                        onValueChange = {},
                        label = { Text("Fin") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedHoraFin) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedHoraFin, onDismissRequest = { expandedHoraFin = false }) {
                        horasDisponibles.filter { it > horaInicioSeleccionada }.forEach { hora ->
                            DropdownMenuItem(
                                text = { Text("$hora:00") },
                                onClick = {
                                    horaFinSeleccionada = hora
                                    expandedHoraFin = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón Guardar
            Button(
                onClick = {
                    if (psicologoSeleccionado != null) {
                        viewModel.guardarHorario(
                            psicologoId = psicologoSeleccionado!!.id, // <-- Usamos .id propio del psicologo (Documento en Firebase)
                            sedesIds = sedesSeleccionadas,
                            diaSemana = diaSeleccionado,
                            horaInicio = horaInicioSeleccionada,
                            horaFin = horaFinSeleccionada
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !uiState.guardando && psicologoSeleccionado != null && sedesSeleccionadas.isNotEmpty()
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