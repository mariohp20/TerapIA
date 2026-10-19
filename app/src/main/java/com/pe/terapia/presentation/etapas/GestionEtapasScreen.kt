package com.pe.terapia.presentation.etapas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pe.terapia.domain.model.EstadoEtapa
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionEtapasScreen(
    procesoId: String,
    onVolver: () -> Unit,
    viewModel: GestionEtapasViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var mostrarDialogoTransicion by remember { mutableStateOf(false) }

    LaunchedEffect(procesoId) {
        viewModel.cargarDatos(procesoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Evolución del Tratamiento") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.etapaEnProgreso != null && !uiState.cargando) {
                ExtendedFloatingActionButton(
                    onClick = { mostrarDialogoTransicion = true },
                    icon = { Icon(Icons.Default.Timeline, contentDescription = null) },
                    text = { Text("Avanzar Etapa") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        if (uiState.cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }

            items(uiState.etapasHistorial) { etapa ->
                val tipoCatalogo = uiState.tiposEtapasCatalogo.find { it.id == etapa.tipoEtapaId }
                val esCompletado = etapa.estado == EstadoEtapa.COMPLETADO

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (esCompletado) Color.White else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (esCompletado) 1.dp else 0.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (esCompletado) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            if (esCompletado) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            } else {
                                Text("▶", color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(tipoCatalogo?.nombre ?: "Etapa Desconocida", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(if (esCompletado) "Completado" else "En Progreso", color = if (esCompletado) Color.Gray else MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)

                            if (etapa.observaciones != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Nota: ${etapa.observaciones}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                            }
                        }
                    }
                }
            }
        }

        // Dialogo para cambiar de etapa
        if (mostrarDialogoTransicion) {
            DialogoTransicionEtapa(
                tiposDisponibles = uiState.tiposEtapasCatalogo,
                guardando = uiState.guardando,
                onDismiss = { mostrarDialogoTransicion = false },
                onConfirmar = { nuevoId, observaciones ->
                    viewModel.avanzarEtapa(procesoId, nuevoId, observaciones)
                    mostrarDialogoTransicion = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoTransicionEtapa(
    tiposDisponibles: List<com.pe.terapia.domain.model.TipoEtapa>,
    guardando: Boolean,
    onDismiss: () -> Unit,
    onConfirmar: (String, String) -> Unit
) {
    var tipoSeleccionado by remember { mutableStateOf(tiposDisponibles.firstOrNull()?.id ?: "") }
    var observaciones by remember { mutableStateOf("") }
    var expandido by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        title = { Text("Transicionar a nueva etapa") },
        text = {
            Column {
                Text("Cierre la etapa actual y seleccione la siguiente.", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = expandido,
                    onExpandedChange = { expandido = !expandido }
                ) {
                    OutlinedTextField(
                        value = tiposDisponibles.find { it.id == tipoSeleccionado }?.nombre ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Siguiente Etapa") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
                        tiposDisponibles.forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text(tipo.nombre) },
                                onClick = {
                                    tipoSeleccionado = tipo.id
                                    expandido = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = observaciones,
                    onValueChange = { observaciones = it },
                    label = { Text("Observaciones de cierre") },
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmar(tipoSeleccionado, observaciones) },
                enabled = !guardando && tipoSeleccionado.isNotBlank()
            ) {
                if (guardando) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                else Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !guardando) { Text("Cancelar") }
        }
    )
}