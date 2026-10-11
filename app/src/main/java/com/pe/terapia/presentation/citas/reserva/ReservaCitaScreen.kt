package com.pe.terapia.presentation.citas.reserva

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pe.terapia.domain.model.Psicologo
import com.pe.terapia.domain.model.Sede
import com.pe.terapia.domain.model.SlotDisponible
import com.pe.terapia.ui.theme.TerapiaTheme
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reserva de cita", style = MaterialTheme.typography.titleLarge) },
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
                    .padding(horizontal = 24.dp, vertical = 16.dp)
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
                        .height(54.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Continuar", style = MaterialTheme.typography.titleMedium)
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
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Elige cómo y cuándo quieres atenderte.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))

            //ESPECIALIDAD
            Text("1. Especialidad", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            if (uiState.cargandoEspecialidades) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (uiState.especialidades.isEmpty()) {
                Text("No hay especialidades configuradas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.especialidades) { especialidad ->
                        val isSelected = uiState.especialidadSeleccionada?.id == especialidad.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.seleccionarEspecialidad(especialidad) },
                            label = { Text(especialidad.nombre) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            uiState.error?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(28.dp))

            //PROFESIONAL
            Text("2. Profesional", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

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
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.Person, contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("${psicologo.nombres} ${psicologo.apellidos}", style = MaterialTheme.typography.titleSmall)
                    }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // SEDE
            Text("3. Sede", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            if (uiState.cargandoSedes) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (uiState.psicologoSeleccionado == null) {
                Text("Selecciona un profesional para consultar sus sedes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else if (uiState.sedes.isEmpty()) {
                Text("Este profesional no tiene sedes activas configuradas.", color = MaterialTheme.colorScheme.error)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.sedes) { sede ->
                        FilterChip(
                            selected = uiState.sedeSeleccionada?.id == sede.id,
                            onClick = { viewModel.seleccionarSede(sede) },
                            label = { Text(sede.nombre) },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            if (uiState.psicologoSeleccionado != null && uiState.sedeSeleccionada != null) {
                Spacer(modifier = Modifier.height(28.dp))
                Text("4. Fecha", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Solo puedes elegir días con turnos disponibles.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(fechasProximas.filter { it.fechaCompleta in uiState.fechasConDisponibilidad }) { fechaItem ->
                        val selected = uiState.fechaSeleccionada == fechaItem.fechaCompleta
                        Surface(
                            modifier = Modifier.width(68.dp).height(82.dp).clickable(enabled = !uiState.cargandoFechas) {
                                viewModel.seleccionarFecha(fechaItem.fechaCompleta)
                            },
                            shape = RoundedCornerShape(18.dp),
                            color = when {
                                selected -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.surface
                            },
                            border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text(fechaItem.diaSemana, style = MaterialTheme.typography.labelMedium, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(fechaItem.diaNumero, style = MaterialTheme.typography.titleLarge, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
                if (uiState.cargandoFechas) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Text("Consultando disponibilidad…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else if (uiState.fechasConsultadas.size == fechasProximas.size && uiState.fechasConDisponibilidad.isEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("No hay turnos disponibles en los próximos días para esta sede.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else if (uiState.fechasConsultadas.size == fechasProximas.size && uiState.fechasConDisponibilidad.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("${uiState.fechasConDisponibilidad.size} días disponibles", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("5. Horario", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                when {
                    uiState.fechaSeleccionada == null -> Text("Selecciona un día disponible para ver las horas.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    uiState.cargandoHorarios -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    uiState.horariosDisponibles.isEmpty() -> Text("Los turnos de ese día ya no están disponibles. Elige otra fecha.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    else -> FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        uiState.horariosDisponibles.forEach { hora ->
                            FilterChip(selected = uiState.horaSeleccionada == hora, onClick = { viewModel.seleccionarHora(hora) }, label = { Text(hora) }, leadingIcon = { Text("◷", style = MaterialTheme.typography.labelLarge) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
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
