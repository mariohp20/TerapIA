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
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.RowChart
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.Line
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardAdminScreen(
    onVolver: () -> Unit,
    viewModel: DashboardAdminViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }
    var busqueda by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { viewModel.cargar() }
    val color = MaterialTheme.colorScheme.primary
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer)) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
            Column(Modifier.weight(1f)) {
                Text("Dashboard de la clínica", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Indicadores administrativos", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box {
                TextButton(onClick = { expanded = true }) { Text(etiquetaPeriodoAdmin(state.meses) + " ▾") }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf(1, 6, 12, 0).forEach { months ->
                        DropdownMenuItem(text = { Text(etiquetaPeriodoAdmin(months)) }, onClick = {
                            expanded = false
                            viewModel.cargar(months)
                        })
                    }
                }
            }
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            color = MaterialTheme.colorScheme.background
        ) { when {
            state.cargando -> androidx.compose.foundation.layout.Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> androidx.compose.foundation.layout.Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.cargar(state.meses) }) { Text("Reintentar") }
                }
            }
            else -> Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IndicadorAdmin("Citas", state.totalCitas.toString(), Modifier.weight(1f))
                    IndicadorAdmin("Atendidas", state.citasCompletadas.toString(), Modifier.weight(1f))
                }
                IndicadorAdmin("Ingresos registrados · PEN", "S/ ${"%.2f".format(state.ingresosPen)}", Modifier.fillMaxWidth())
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Ingresos por mes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Pagos registrados como pagados en soles", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        if (state.ingresosPorMes.all { it.cantidad == 0.0 }) TextoSinDatos("Aún no hay pagos pagados en este período.")
                        else ColumnChart(
                            Modifier.fillMaxWidth().height(220.dp),
                            data = state.ingresosPorMes.map { item ->
                                Bars(label = item.etiqueta, values = listOf(Bars.Data(value = item.cantidad, color = SolidColor(color))))
                            }
                        )
                    }
                }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Citas programadas y atendidas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Comparación por mes · citas completadas cuentan como atendidas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        if (state.citasPorMes.all { it.second.first == 0 }) TextoSinDatos("No hay citas en este período.")
                        else LineChart(
                            Modifier.fillMaxWidth().height(220.dp),
                            data = listOf(
                                Line("Programadas", state.citasPorMes.map { it.second.first.toDouble() }, SolidColor(MaterialTheme.colorScheme.outline)),
                                Line("Atendidas", state.citasPorMes.map { it.second.second.toDouble() }, SolidColor(color))
                            )
                        )
                    }
                }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Atenciones por psicólogo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Solo citas con estado COMPLETADA", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = busqueda,
                            onValueChange = { busqueda = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Buscar psicólogo") },
                            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) }
                        )
                        val filtered = state.cargaPorPsicologo.filter { it.nombre.contains(busqueda, ignoreCase = true) }
                        Spacer(Modifier.height(12.dp))
                        if (filtered.isEmpty()) TextoSinDatos(if (busqueda.isBlank()) "Todavía no hay atenciones registradas." else "No hay resultados para esa búsqueda.")
                        else RowChart(
                            Modifier.fillMaxWidth().height((filtered.size * 58).coerceIn(150, 360).dp),
                            data = filtered.map { item ->
                                Bars(label = item.nombre, values = listOf(Bars.Data(value = item.atenciones.toDouble(), color = SolidColor(color))))
                            }
                        )
                    }
                }
            }
        } }
    }
}

private fun etiquetaPeriodoAdmin(meses: Int): String = when (meses) {
    0 -> "Todo"
    1 -> "Este mes"
    else -> "$meses meses"
}

@Composable
private fun IndicadorAdmin(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Card(modifier) { Column(Modifier.padding(18.dp)) {
        Text(titulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(valor, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    } }
}

@Composable
private fun TextoSinDatos(texto: String) {
    androidx.compose.foundation.layout.Box(Modifier.fillMaxWidth().height(110.dp), contentAlignment = Alignment.Center) {
        Text(texto, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
