package com.pe.terapia.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pe.terapia.presentation.dashboard.components.TerapiaColumnChart
import com.pe.terapia.presentation.dashboard.components.TerapiaLineChart
import com.pe.terapia.presentation.dashboard.components.TerapiaPieChart
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.Line
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardPsicologoScreen(
    onVolver: () -> Unit,
    viewModel: DashboardPsicologoViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var filtroExpandido by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.cargarDashboard()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        // Cabecera Principal con Filtro
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onVolver,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.65f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Mi Rendimiento",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Métricas operativas de la clínica",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                modifier = Modifier.padding(start = 64.dp)
            )

            // Selector de Filtro de Tiempo
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box {
                    TextButton(onClick = { filtroExpandido = true }) {
                        Text(when (uiState.mesesFiltro) {
                            0 -> "Todo el historial ▾"
                            1 -> "Este mes ▾"
                            else -> "Últimos ${uiState.mesesFiltro} meses ▾"
                        })
                    }
                    DropdownMenu(expanded = filtroExpandido, onDismissRequest = { filtroExpandido = false }) {
                        listOf(1 to "Este mes", 3 to "Últimos 3 meses", 6 to "Últimos 6 meses", 0 to "Todo el historial")
                            .forEach { (meses, etiqueta) ->
                                DropdownMenuItem(
                                    text = { Text(etiqueta) },
                                    onClick = {
                                        filtroExpandido = false
                                        viewModel.cargarDashboard(meses)
                                    }
                                )
                            }
                    }
                }
            }
        }

        // Superficie de contenido
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                uiState.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                uiState.error != null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(uiState.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                }
                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // TARJETA 0: Resumen Global
                    val totalAtenciones = uiState.atencionesPorMes.values.sum()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Atenciones realizadas", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                totalAtenciones.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(textoPeriodo(uiState.mesesFiltro), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // TARJETA 1: ColumnChart (Volumen de Atenciones)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Text("Volumen de Atenciones", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text("Sesiones realizadas históricamente", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(24.dp))

                            val primaryColor = MaterialTheme.colorScheme.primary

                            if (uiState.atencionesPorMes.isEmpty() || totalAtenciones == 0) {
                                SinDatosPlaceholder("Aún no tienes atenciones registradas")
                            } else {
                                TerapiaColumnChart(
                                    modifier = Modifier.fillMaxWidth().height(250.dp),
                                    data = remember(uiState.atencionesPorMes, primaryColor) {
                                        uiState.atencionesPorMes.map { (mes, cantidad) ->
                                            Bars(
                                                label = mes,
                                                values = listOf(
                                                    Bars.Data(
                                                        label = "Atenciones",
                                                        value = cantidad.toDouble(),
                                                        color = SolidColor(primaryColor)
                                                    )
                                                )
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // TARJETA 2: PieChart (Estado de Pacientes) - ¡NUEVO COMPONENTE!
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Text("Estado de Pacientes Activos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text("Proporción por fase terapéutica", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(24.dp))

                            if (uiState.pacientesPorEtapa.isEmpty()) {
                                SinDatosPlaceholder("No hay pacientes en progreso")
                            } else {
                                TerapiaPieChart(
                                    modifier = Modifier.fillMaxWidth().height(260.dp),
                                    datos = uiState.pacientesPorEtapa
                                )
                            }
                        }
                    }

                    // TARJETA 3: LineChart (Adherencia y Ausentismo)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Text("Adherencia y Ausentismo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text("Citas programadas vs. Atenciones realizadas", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(24.dp))

                            val colorAtenciones = MaterialTheme.colorScheme.primary
                            val colorCitas = MaterialTheme.colorScheme.outline

                            if (uiState.comparativaAsistencia.all { it.programadas == 0 }) {
                                SinDatosPlaceholder("Faltan datos de citas para generar la comparativa")
                            } else {
                                TerapiaLineChart(
                                    modifier = Modifier.fillMaxWidth().height(220.dp),
                                    labels = uiState.comparativaAsistencia.map { it.mes },
                                    data = remember(uiState.comparativaAsistencia, colorAtenciones, colorCitas) {
                                        listOf(
                                            Line(
                                                label = "Programadas",
                                                values = uiState.comparativaAsistencia.map { it.programadas.toDouble() },
                                                color = SolidColor(colorCitas)
                                            ),
                                            Line(
                                                label = "Realizadas",
                                                values = uiState.comparativaAsistencia.map { it.realizadas.toDouble() },
                                                color = SolidColor(colorAtenciones)
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

private fun textoPeriodo(meses: Int): String = when (meses) {
    0 -> "Todo el historial"
    1 -> "Este mes"
    else -> "Últimos $meses meses"
}

@Composable
fun SinDatosPlaceholder(mensaje: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(mensaje, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}