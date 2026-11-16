package com.pe.terapia.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Search
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
import com.pe.terapia.presentation.dashboard.components.TerapiaRowChart
import com.pe.terapia.presentation.dashboard.components.obtenerPaletaGraficos
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.Line
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardAdminScreen(
    onVolver: () -> Unit,
    viewModel: DashboardAdminViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var filtroExpandido by remember { mutableStateOf(false) }
    var busqueda by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.cargarDashboard()
    }

    val color = MaterialTheme.colorScheme.primary

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
            Column(Modifier.weight(1f)) {
                Text(
                    "Dashboard de la clínica",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Indicadores administrativos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box {
                TextButton(onClick = {
                    filtroExpandido = true
                }) { Text(etiquetaPeriodoFiltro(state.meses) + " ▾") }
                DropdownMenu(expanded = filtroExpandido, onDismissRequest = { filtroExpandido = false }) {
                    listOf(1, 3, 6, 0).forEach { meses ->
                        DropdownMenuItem(text = { Text(etiquetaPeriodoFiltro(meses)) }, onClick = {
                            filtroExpandido = false
                            viewModel.cargarDashboard(meses)
                        })
                    }
                }
            }
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                state.cargando -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                state.error != null -> Box(
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp), contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.cargarDashboard(state.meses) }) { Text("Reintentar") }
                    }
                }

                else -> Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IndicadorAdmin("Citas", state.totalCitas.toString(), Modifier.weight(1f))
                        IndicadorAdmin(
                            "Atendidas",
                            state.citasCompletadas.toString(),
                            Modifier.weight(1f)
                        )
                    }
                    IndicadorAdmin(
                        "Ingresos registrados · PEN",
                        "S/ ${"%.2f".format(state.ingresosPen)}",
                        Modifier.fillMaxWidth()
                    )
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp)) {
                            Text(
                                "Ingresos por mes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Pagos registrados como pagados en soles",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            if (state.ingresosPorMes.all { it.cantidad == 0.0 }) TextoSinDatos("Aún no hay pagos pagados en este período.")
                            else TerapiaColumnChart(
                                modifier = Modifier.fillMaxWidth().height(220.dp),
                                data = remember(state.ingresosPorMes, color) {
                                    state.ingresosPorMes.map { item ->
                                        Bars(
                                            label = item.etiqueta,
                                            values = listOf(
                                                Bars.Data(
                                                    label = "Ingresos",
                                                    value = item.cantidad,
                                                    color = SolidColor(color)
                                                )
                                            )
                                        )
                                    }
                                }
                            )
                        }
                    }
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp)) {
                            Text(
                                "Citas programadas y atendidas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Comparación por mes · citas completadas cuentan como atendidas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            if (state.citasPorMes.all { it.programadas == 0 }) TextoSinDatos("No hay citas en este período.")
                            else {
                                val paleta = obtenerPaletaGraficos()
                                TerapiaLineChart(
                                    modifier = Modifier.fillMaxWidth().height(220.dp),
                                    labels = state.citasPorMes.map { it.etiqueta },
                                    data = remember(state.citasPorMes, paleta) {
                                        listOf(
                                            Line(
                                                label = "Programadas",
                                                values = state.citasPorMes.map { it.programadas.toDouble() },
                                                color = SolidColor(paleta[2])
                                            ),
                                            Line(
                                                label = "Atendidas",
                                                values = state.citasPorMes.map { it.atendidas.toDouble() },
                                                color = SolidColor(paleta[0])
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp)) {
                            Text(
                                "Atenciones por psicólogo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Solo citas con estado COMPLETADA",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = busqueda,
                                onValueChange = { busqueda = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                label = { Text("Buscar psicólogo") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Search,
                                        contentDescription = null
                                    )
                                }
                            )
                            val filtered = state.cargaPorPsicologo.filter {
                                it.nombre.contains(
                                    busqueda,
                                    ignoreCase = true
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            if (filtered.isEmpty()) TextoSinDatos(if (busqueda.isBlank()) "Todavía no hay atenciones registradas." else "No hay resultados para esa búsqueda.")
                            else TerapiaRowChart(
                                modifier = Modifier.fillMaxWidth().height((filtered.size * 58).coerceIn(150, 360).dp),
                                data = remember(filtered, color) {
                                    filtered.map { item ->
                                        Bars(
                                            label = item.nombre,
                                            values = listOf(
                                                Bars.Data(
                                                    label = "Atenciones",
                                                    value = item.atenciones.toDouble(),
                                                    color = SolidColor(color)
                                                )
                                            )
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun etiquetaPeriodoFiltro(meses: Int): String = when (meses) {
    0 -> "Todo"
    1 -> "Este mes"
    else -> "$meses meses"
}

@Composable
private fun IndicadorAdmin(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(18.dp)) {
            Text(
                titulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                valor,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TextoSinDatos(texto: String) {
    Box(Modifier
        .fillMaxWidth()
        .height(110.dp), contentAlignment = Alignment.Center) {
        Text(texto, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
// Si se piensa cambiar a 12 meses
/*
@Composable
private fun DeslizadorGrafico(scrollState: androidx.compose.foundation.ScrollState, visible: Boolean) {
    if (!visible) return
    val scope = rememberCoroutineScope()
    val maximo = scrollState.maxValue.coerceAtLeast(1)
    Column {
        Slider(
            value = scrollState.value.toFloat().coerceIn(0f, maximo.toFloat()),
            onValueChange = { valor -> scope.launch { scrollState.scrollTo(valor.roundToInt()) } },
            valueRange = 0f..maximo.toFloat(),
            modifier = Modifier.fillMaxWidth()
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Más antiguo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Más reciente", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}*/