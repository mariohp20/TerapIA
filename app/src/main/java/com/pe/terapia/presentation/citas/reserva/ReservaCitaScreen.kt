package com.pe.terapia.presentation.citas.reserva

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pe.terapia.domain.model.Psicologo
import com.pe.terapia.domain.model.Sede
import com.pe.terapia.domain.model.SlotDisponible
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReservaCitaScreen(
    onContinuar: (psicologo: Psicologo, especialidadId: String, especialidadNombre: String, sede: Sede, slot: SlotDisponible) -> Unit,
    onVolver: () -> Unit,
    viewModel: ReservaCitaViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val fechasProximas = remember { generarFechasProximas(14) }

    LaunchedEffect(uiState.especialidades) {
        if (uiState.especialidadSeleccionada == null && uiState.especialidades.isNotEmpty()) {
            viewModel.seleccionarEspecialidad(uiState.especialidades.first())
        }
    }

    // Determinar el paso actual para el Stepper
    val pasoActual = when {
        uiState.sedeSeleccionada != null -> 2
        uiState.psicologoSeleccionado != null -> 1
        else -> 1
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reservar Cita", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.background
            ) {
                Button(
                    onClick = {
                        val psicologo = uiState.psicologoSeleccionado
                        val especialidad = uiState.especialidadSeleccionada
                        val sede = uiState.sedeSeleccionada
                        val slot = viewModel.slotSeleccionado()

                        if (psicologo != null && especialidad != null && sede != null && slot != null) {
                            onContinuar(psicologo, especialidad.id, especialidad.nombre, sede, slot)
                        }
                    },
                    enabled = uiState.puedeContinuar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Continuar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // COMPONENTE STEPPER
            IndicadorPasos(pasoActual = pasoActual)
            Spacer(modifier = Modifier.height(24.dp))

            // ESPECIALIDAD
            Text("Selecciona una especialidad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            if (uiState.cargandoEspecialidades) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.especialidades) { especialidad ->
                        val isSelected = uiState.especialidadSeleccionada?.id == especialidad.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.seleccionarEspecialidad(especialidad) },
                            label = { Text(especialidad.nombre, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PROFESIONAL
            Text("Selecciona un profesional", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            if (uiState.cargandoPsicologos) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (uiState.psicologos.isEmpty()) {
                Text("No hay profesionales disponibles.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.psicologos) { psicologo ->
                        val isSelected = uiState.psicologoSeleccionado?.id == psicologo.id
                        OutlinedCard(
                            onClick = { viewModel.seleccionarPsicologo(psicologo) },
                            modifier = Modifier.width(220.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.Person, contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Ps. ${psicologo.nombres.split(" ").first()} ${psicologo.apellidos.split(" ").first()}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(uiState.especialidadSeleccionada?.nombre ?: "Psicólogo", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // SEDE
            if (uiState.psicologoSeleccionado != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Selecciona una sede", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                if (uiState.cargandoSedes) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        uiState.sedes.forEach { sede ->
                            val isSelected = uiState.sedeSeleccionada?.id == sede.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.seleccionarSede(sede) },
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column {
                                            Text(sede.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            Text(sede.direccion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Icon(
                                        imageVector = if (isSelected) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // FECHA Y HORARIO
            if (uiState.psicologoSeleccionado != null && uiState.sedeSeleccionada != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Selecciona fecha y horario", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                if (uiState.cargandoFechas) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("Buscando días disponibles…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Lista Horizontal de Días
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(fechasProximas.filter { it.fechaCompleta in uiState.fechasConDisponibilidad }) { fechaItem ->
                        val selected = uiState.fechaSeleccionada == fechaItem.fechaCompleta
                        Surface(
                            modifier = Modifier
                                .width(64.dp)
                                .height(78.dp)
                                .clickable { viewModel.seleccionarFecha(fechaItem.fechaCompleta) },
                            shape = RoundedCornerShape(18.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text(fechaItem.diaSemana, style = MaterialTheme.typography.labelMedium, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(fechaItem.diaNumero, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                // Lista de Horarios
                if (uiState.fechaSeleccionada != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Horarios disponibles", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    when {
                        uiState.cargandoHorarios -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        uiState.horariosDisponibles.isEmpty() -> Text("Sin turnos para este día.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        else -> FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            uiState.horariosDisponibles.forEach { hora ->
                                FilterChip(
                                    selected = uiState.horaSeleccionada == hora,
                                    onClick = { viewModel.seleccionarHora(hora) },
                                    label = { Text(hora, fontWeight = FontWeight.Medium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// COMPONENTE STEPPER
@Composable
private fun IndicadorPasos(pasoActual: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PasoItem(numero = "1", etiqueta = "Psicólogo", activo = pasoActual >= 1)
        Divider(modifier = Modifier.weight(0.2f), color = MaterialTheme.colorScheme.primaryContainer)
        PasoItem(numero = "2", etiqueta = "Sede y Fecha", activo = pasoActual >= 2)
        Divider(modifier = Modifier.weight(0.2f), color = MaterialTheme.colorScheme.primaryContainer)
        PasoItem(numero = "3", etiqueta = "Confirmación", activo = pasoActual >= 3)
    }
}

@Composable
private fun PasoItem(numero: String, etiqueta: String, activo: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = if (activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = numero,
                color = if (activo) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = if (activo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal
        )
    }
}

data class FechaItem(val diaSemana: String, val diaNumero: String, val fechaCompleta: String)

private fun generarFechasProximas(dias: Int): List<FechaItem> {
    val fechas = mutableListOf<FechaItem>()
    val hoy = LocalDate.now(java.time.ZoneId.of("America/Lima"))
    val localeSpanish = Locale.forLanguageTag("es-ES")

    for (i in 0 until dias) {
        val fecha = hoy.plusDays(i.toLong())
        val diaSemana = fecha.dayOfWeek.getDisplayName(TextStyle.SHORT, localeSpanish).replaceFirstChar { it.uppercase() }
        val diaNumero = fecha.dayOfMonth.toString()
        val fechaCompleta = fecha.format(DateTimeFormatter.ISO_LOCAL_DATE)
        fechas.add(FechaItem(diaSemana, diaNumero, fechaCompleta))
    }
    return fechas
}