package com.pe.terapia.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pe.terapia.domain.model.ComparativaMensualPsicologo
import com.pe.terapia.domain.model.ConteoEtapaPsicologo
import com.pe.terapia.domain.model.ConteoMensualPsicologo
import com.pe.terapia.ui.theme.TerapiaTheme
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.Pie
import org.koin.androidx.compose.koinViewModel
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.Line

@Composable
fun DashboardPsicologoScreen(
    onVolver: () -> Unit,
    viewModel: DashboardPsicologoViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.cargarDashboard()
    }

    DashboardPsicologoContent(
        uiState = uiState,
        onVolver = onVolver,
        onCambiarPeriodo = viewModel::cargarDashboard
    )
}

@Composable
fun DashboardPsicologoContent(
    uiState: DashboardPsicologoUiState,
    onVolver: () -> Unit,
    onCambiarPeriodo: (Int) -> Unit = {}
) {
    var filtroExpandido by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onVolver,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.65f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Mi Rendimiento",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Métricas operativas del mes",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                )
            }
            Box {
                TextButton(onClick = { filtroExpandido = true }) { Text(etiquetaPeriodoPsicologo(uiState.mesesFiltro)) }
                DropdownMenu(expanded = filtroExpandido, onDismissRequest = { filtroExpandido = false }) {
                    listOf(1 to "Este mes", 6 to "6 meses", 12 to "12 meses", 0 to "Todo").forEach { (meses, etiqueta) ->
                        DropdownMenuItem(text = { Text(etiqueta) }, onClick = { filtroExpandido = false; onCambiarPeriodo(meses) })
                    }
                }
            }
        }

        // Superficie de contenido
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            if (uiState.cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Surface
            }

            if (uiState.error != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.error, color = MaterialTheme.colorScheme.error)
                }
                return@Surface
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(36.dp)
            ) {
                // ColumnChar: Volumen de Atenciones
                Column {
                    Text(
                        text = "Volumen de Atenciones",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Sesiones realizadas históricamente",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val primaryColor = MaterialTheme.colorScheme.primary

                    if (uiState.atencionesPorMes.isEmpty()) {
                        SinDatosPlaceholder("Aún no tienes atenciones registradas")
                    } else {
                        val scrollState = rememberScrollState()
                        val requiereDesplazamiento = uiState.atencionesPorMes.size > 6
                        LaunchedEffect(uiState.atencionesPorMes) { if (requiereDesplazamiento) scrollState.animateScrollTo(scrollState.maxValue) }
                        Box(Modifier.fillMaxWidth().then(if (requiereDesplazamiento) Modifier.horizontalScroll(scrollState) else Modifier)) {
                        ColumnChart(
                            modifier = (if (requiereDesplazamiento) Modifier.width((uiState.atencionesPorMes.size * 72).dp) else Modifier.fillMaxWidth()).height(250.dp),
                            data = remember(uiState.atencionesPorMes, primaryColor) {
                                uiState.atencionesPorMes.map { item ->
                                    Bars(
                                        label = item.etiqueta,
                                        values = listOf(Bars.Data(value = item.cantidad.toDouble(), color = SolidColor(primaryColor)))
                                    )
                                }
                            }
                        )
                        }
                        DeslizadorMesesPsicologo(scrollState, requiereDesplazamiento)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                // PieChart: Estado de Pacientes Activos
                Column {
                    Text(
                        text = "Estado de Pacientes Activos",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Proporción por fase terapéutica",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    val colorPrimario = MaterialTheme.colorScheme.primary
                    val colorSecundario = MaterialTheme.colorScheme.tertiary
                    val colorTerciario = MaterialTheme.colorScheme.secondary
                    val colores = listOf(colorPrimario, colorSecundario, colorTerciario)

                    if (uiState.pacientesPorEtapa.isEmpty()) {
                        SinDatosPlaceholder("No hay pacientes en progreso actualmente")
                    } else {
                        PieChart(
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                            data = remember(uiState.pacientesPorEtapa, colorPrimario) {
                                uiState.pacientesPorEtapa.mapIndexed { index, item ->
                                    Pie(
                                        label = item.nombre,
                                        data = item.cantidad.toDouble(),
                                        color = colores.getOrElse(index % colores.size) { Color.Gray },
                                        selectedColor = Color.DarkGray
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Componente de Leyenda Desacoplado
                        LeyendaPieChart(
                            datos = uiState.pacientesPorEtapa.associate { it.nombre to it.cantidad },
                            colores = colores
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                // LineChart: Adherencia y Ausentismo
                Column {
                    Text(
                        text = "Adherencia y Ausentismo",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Citas programadas vs. Atenciones realizadas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    val colorAtenciones = MaterialTheme.colorScheme.primary
                    val colorCitas = MaterialTheme.colorScheme.outline

                    if (uiState.comparativaAsistencia.all { it.citas == 0 }) {
                        SinDatosPlaceholder("Faltan datos de citas para generar la comparativa")
                    } else {
                        LineChart(
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                            data = remember(uiState.comparativaAsistencia, colorAtenciones, colorCitas) {
                                listOf(
                                    Line(
                                        label = "Programadas",
                                        values = uiState.comparativaAsistencia.map { it.citas.toDouble() },
                                        color = SolidColor(colorCitas)
                                    ),
                                    Line(
                                        label = "Realizadas",
                                        values = uiState.comparativaAsistencia.map { it.atenciones.toDouble() },
                                        color = SolidColor(colorAtenciones)
                                    )
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Leyenda Manual Desacoplada
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(colorCitas))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Programadas", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(modifier = Modifier.width(24.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(colorAtenciones))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Realizadas", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

private fun etiquetaPeriodoPsicologo(meses: Int) = when (meses) {
    0 -> "Todo ▾"
    1 -> "Este mes ▾"
    else -> "$meses meses ▾"
}

@Composable
private fun DeslizadorMesesPsicologo(scrollState: androidx.compose.foundation.ScrollState, visible: Boolean) {
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LeyendaPieChart(
    datos: Map<String, Int>,
    colores: List<Color>
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        datos.entries.mapIndexed { index, (etapa, cantidad) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(colores.getOrElse(index % colores.size) { Color.Gray })
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$etapa ($cantidad)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// Placeholder, cuando no se tienen datos
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

@Preview(showBackground = true)
@Composable
fun DashboardPsicologoPreview() {
    TerapiaTheme {
        DashboardPsicologoContent(
            uiState = DashboardPsicologoUiState(
                cargando = false,
                atencionesPorMes = listOf(ConteoMensualPsicologo("2026-07", "Jul 26", 15), ConteoMensualPsicologo("2026-08", "Ago 26", 30)),
                pacientesPorEtapa = listOf(ConteoEtapaPsicologo("Evaluación", 10), ConteoEtapaPsicologo("Intervención", 25), ConteoEtapaPsicologo("Cierre", 5)),
                comparativaAsistencia = listOf(
                    ComparativaMensualPsicologo("2026-07", "Jul 26", 20, 15),
                    ComparativaMensualPsicologo("2026-08", "Ago 26", 35, 30)
                )
            ),
            onVolver = {}
        )
    }
}
