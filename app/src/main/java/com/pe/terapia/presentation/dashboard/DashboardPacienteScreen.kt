package com.pe.terapia.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pe.terapia.presentation.dashboard.components.TerapiaPieChart
import com.pe.terapia.presentation.dashboard.components.TerapiaRowChart
import ir.ehsannarmani.compose_charts.models.Bars
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardPacienteScreen(
    onVolver: () -> Unit,
    viewModel: DashboardPacienteViewModel = koinViewModel()
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
                    "Mi progreso",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Un resumen de tus sesiones y asistencia",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                TextButton(onClick = {
                    filtroExpandido = true
                }) { Text(etiquetaPeriodoFiltro(uiState.mesesFiltro) + " ▾") }
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
                uiState.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                uiState.error != null -> Box(
                    Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                }
                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // TARJETA 1: Resumen General
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Atenciones registradas", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                uiState.totalAtenciones.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(textoPeriodo(uiState.mesesFiltro), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // TARJETA 2: Row Chart (Atenciones por mes)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Text("Atenciones por mes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text("Registros clínicos · ${textoPeriodo(uiState.mesesFiltro).lowercase()}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(20.dp))

                            val primaryColor = MaterialTheme.colorScheme.primary

                            if (uiState.totalAtenciones == 0) {
                                SinDatosPaciente("Aún no hay sesiones registradas para mostrar")
                            } else {
                                TerapiaRowChart(
                                    modifier = Modifier.fillMaxWidth().height((uiState.asistenciasPorMes.size * 58).coerceIn(150, 360).dp),
                                    data = remember(uiState.asistenciasPorMes, primaryColor) {
                                        uiState.asistenciasPorMes.map { mes ->
                                            Bars(
                                                label = mes.etiqueta,
                                                values = listOf(
                                                    Bars.Data(
                                                        label = "Atenciones",
                                                        value = mes.cantidad.toDouble(),
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

                    // TARJETA 3: Pie Chart (Estado de tus citas)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Column(Modifier.padding(18.dp)) {
                            Text("Estado de tus citas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text("Las atenciones registradas cuentan como completadas; los demás resultados dependen del estado de la cita", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(20.dp))

                            if (uiState.estadosHistoricos.all { it.cantidad == 0 }) {
                                SinDatosPaciente("Todavía no hay resultados de citas para mostrar")
                            } else {
                                val datosMapa = remember(uiState.estadosHistoricos) {
                                    uiState.estadosHistoricos.filter { it.cantidad > 0 }.associate { it.etiqueta to it.cantidad }
                                }

                                TerapiaPieChart(
                                    modifier = Modifier.fillMaxWidth().height(260.dp),
                                    datos = datosMapa
                                )
                            }
                        }
                    }
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
private fun etiquetaPeriodoFiltro(meses: Int): String = when (meses) {
    0 -> "Todo"
    1 -> "Este mes"
    else -> "$meses meses"
}
@Composable
private fun SinDatosPaciente(mensaje: String) {
    Box(
        Modifier.fillMaxWidth().height(150.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(mensaje, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}