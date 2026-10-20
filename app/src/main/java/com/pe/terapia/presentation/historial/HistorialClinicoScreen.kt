package com.pe.terapia.presentation.historial

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pe.terapia.R
import com.pe.terapia.domain.model.Atencion
import com.pe.terapia.domain.model.EstadoEtapa
import com.pe.terapia.domain.model.EtapaClinica
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import org.koin.androidx.compose.koinViewModel
import com.pe.terapia.domain.model.TipoEtapa

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialClinicoScreen(
    pacienteId: String,
    pacienteNombre: String,
    onVolver: () -> Unit,
    viewModel: HistorialClinicoViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Estado para las pestañas
    val tabs = listOf("Resumen", "Sesiones", "Notas", "Etapas")
    var tabSeleccionado by remember { mutableStateOf(0) }

    LaunchedEffect(pacienteId) {
        viewModel.cargarHistorial(pacienteId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.historia_clinica), fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.volver))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Cabecera del Paciente
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(pacienteNombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fila deslizable de pestañas (TabRow)
            PrimaryScrollableTabRow(
                selectedTabIndex = tabSeleccionado,
                edgePadding = 24.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = tabSeleccionado == index,
                        onClick = { tabSeleccionado = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (tabSeleccionado == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (tabSeleccionado == index) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Contenido dinámico según la pestaña
            if (uiState.cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (tabSeleccionado) {
                    0 -> ResumenTab(
                        etapaActivaNombre = uiState.etapaActivaNombre,
                        ultimaAtencion = uiState.atenciones.firstOrNull()
                    )
                    1 -> SesionesTab(atenciones = uiState.atenciones, esPsicologo = uiState.esPsicologo)
                    3 -> EtapasTab(etapas = uiState.historialEtapas, catalogo = uiState.tiposEtapas)
                    else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.proximamente), color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun ResumenTab(
    etapaActivaNombre: String,
    ultimaAtencion: Atencion?
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp)) {

        // Bloque Fase Actual
        Text(stringResource(R.string.fase_actual), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.DarkGray)
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Diseño flat, sin sombra
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = etapaActivaNombre,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.Timeline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Bloque Última Sesión
        Text(stringResource(R.string.ultima_sesion), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.DarkGray)
        Spacer(modifier = Modifier.height(12.dp))

        if (ultimaAtencion != null) {
            val formateador = DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm").withZone(ZoneId.systemDefault())
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(formateador.format(ultimaAtencion.fechaAtencion), fontWeight = FontWeight.Medium, color = Color.DarkGray)
                }
            }
        } else {
            Text(stringResource(R.string.no_hay_sesiones_previas_registradas), color = Color.Gray)
        }
    }
}

@Composable
fun SesionesTab(atenciones: List<Atencion>, esPsicologo: Boolean) {
    if (atenciones.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay atenciones registradas.", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(atenciones) { atencion ->
                AtencionCard(atencion, esPsicologo)
            }
        }
    }
}

@Composable
fun AtencionCard(atencion: Atencion, esPsicologo: Boolean) {
    val formateador = DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm").withZone(ZoneId.systemDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = formateador.format(atencion.fechaAtencion), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            // Campo General
            if (atencion.motivoConsulta.isNotBlank()) {
                Text("Motivo de consulta", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.DarkGray)
                Text(atencion.motivoConsulta, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Solo Psicólogo
            if (esPsicologo && atencion.observaciones.isNotBlank()) {
                Text("Observaciones", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.DarkGray)
                Text(atencion.observaciones, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Campo General
            if (atencion.evaluacion.isNotBlank()) {
                Text("Evaluación", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.DarkGray)
                Text(atencion.evaluacion, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Solo Psicólogo
            if (esPsicologo && atencion.evolucion.isNotBlank()) {
                Text("Evolución del paciente", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.DarkGray)
                Text(atencion.evolucion, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Campo General
            if (atencion.indicaciones.isNotBlank()) {
                Text("Indicaciones", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.DarkGray)
                Text(atencion.indicaciones, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
    }
}

@Composable
fun EtapasTab(etapas: List<EtapaClinica>, catalogo: List<TipoEtapa>) {
    if (etapas.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay etapas clínicas registradas.", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(etapas) { etapa ->
                val tipo = catalogo.find { it.id == etapa.tipoEtapaId }
                EtapaHistorialCard(etapa, tipo)
            }
        }
    }
}

@Composable
fun EtapaHistorialCard(etapa: EtapaClinica, tipoEtapa: TipoEtapa?) {
    val formateador = DateTimeFormatter.ofPattern("dd MMM yyyy").withZone(ZoneId.systemDefault())
    val esCompletado = etapa.estado == EstadoEtapa.COMPLETADO

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Fechas
            val fechaInicio = formateador.format(etapa.fechaInicio)
            val fechaFin = etapa.fechaFin?.let { formateador.format(it) } ?: "Actualidad"

            Text(
                text = "$fechaInicio - $fechaFin",
                style = MaterialTheme.typography.labelMedium,
                color = if (esCompletado) Color.Gray else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Nombre de la Etapa y Estado
            Text(tipoEtapa?.nombre ?: "Etapa Desconocida", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.DarkGray)
            Text(
                text = if (esCompletado) "Completado" else "En Progreso",
                style = MaterialTheme.typography.bodyMedium,
                color = if (esCompletado) Color.Gray else MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Observaciones de cierre (solo si existen)
            if (!etapa.observaciones.isNullOrBlank()) {
                Text("Observaciones de cierre", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.DarkGray)
                Text(etapa.observaciones, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
    }
}