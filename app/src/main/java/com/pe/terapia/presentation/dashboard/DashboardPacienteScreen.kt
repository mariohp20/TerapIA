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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Text(
                text = "Mi progreso",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Un resumen de tus sesiones y asistencia",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
            )
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
                        listOf(1 to "Este mes", 6 to "Últimos 6 meses", 12 to "Últimos 12 meses", 0 to "Todo el historial")
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
                            if (uiState.totalAtenciones == 0) {
                                SinDatosPaciente("Aún no hay sesiones registradas para mostrar")
                            } else {
                                val maximo = uiState.asistenciasPorMes.maxOfOrNull { it.cantidad }?.coerceAtLeast(1) ?: 1
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    uiState.asistenciasPorMes.forEach { mes ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(mes.etiqueta, modifier = Modifier.width(48.dp), style = MaterialTheme.typography.bodyMedium)
                                            Box(
                                                Modifier.weight(1f).height(18.dp).clip(RoundedCornerShape(9.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                                            ) {
                                                if (mes.cantidad > 0) Box(
                                                    Modifier.fillMaxWidth(mes.cantidad.toFloat() / maximo)
                                                        .height(18.dp).clip(RoundedCornerShape(9.dp))
                                                        .background(MaterialTheme.colorScheme.primary)
                                                )
                                            }
                            Text("${mes.cantidad}", modifier = Modifier.width(32.dp).padding(start = 8.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }

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
                            val colores = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary,
                                MaterialTheme.colorScheme.error,
                                MaterialTheme.colorScheme.secondary
                            )
                            if (uiState.estadosHistoricos.all { it.cantidad == 0 }) {
                                SinDatosPaciente("Todavía no hay resultados de citas para mostrar")
                            } else {
                                val colorSeleccionado = MaterialTheme.colorScheme.secondary
                                PieChart(
                                    modifier = Modifier.fillMaxWidth().height(220.dp),
                                    data = remember(uiState.estadosHistoricos, colores) {
                                        uiState.estadosHistoricos.mapIndexedNotNull { index, estado ->
                                            if (estado.cantidad <= 0) return@mapIndexedNotNull null
                                            Pie(
                                                label = estado.etiqueta,
                                                data = estado.cantidad.toDouble(),
                                                color = colores[index],
                                                selectedColor = colorSeleccionado
                                            )
                                        }
                                    }
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                uiState.estadosHistoricos.forEachIndexed { index, estado ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(12.dp).clip(CircleShape).background(colores[index % colores.size]))
                                        Spacer(Modifier.size(8.dp))
                                        Text("${estado.etiqueta}: ${estado.cantidad}", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
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

@Composable
private fun SinDatosPaciente(mensaje: String) {
    Box(
        Modifier.fillMaxWidth().height(150.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(mensaje, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
