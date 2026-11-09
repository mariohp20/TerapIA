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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        onVolver = onVolver
    )
}

@Composable
fun DashboardPsicologoContent(
    uiState: DashboardPsicologoUiState,
    onVolver: () -> Unit
) {
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
            Column {
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
                        ColumnChart(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            data = remember(uiState.atencionesPorMes, primaryColor) {
                                uiState.atencionesPorMes.map { (mes, cantidad) ->
                                    Bars(
                                        label = mes,
                                        values = listOf(Bars.Data(value = cantidad.toDouble(), color = SolidColor(primaryColor)))
                                    )
                                }
                            }
                        )
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
                                uiState.pacientesPorEtapa.entries.mapIndexed { index, (etapa, cantidad) ->
                                    Pie(
                                        label = etapa,
                                        data = cantidad.toDouble(),
                                        color = colores.getOrElse(index % colores.size) { Color.Gray },
                                        selectedColor = Color.DarkGray
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Componente de Leyenda Desacoplado
                        LeyendaPieChart(
                            datos = uiState.pacientesPorEtapa,
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

                    if (uiState.comparativaAsistencia.all { it.second == 0 }) {
                        SinDatosPlaceholder("Faltan datos de citas para generar la comparativa")
                    } else {
                        LineChart(
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                            data = remember(uiState.comparativaAsistencia, colorAtenciones, colorCitas) {
                                listOf(
                                    Line(
                                        label = "Programadas",
                                        values = uiState.comparativaAsistencia.map { it.second.toDouble() },
                                        color = SolidColor(colorCitas)
                                    ),
                                    Line(
                                        label = "Realizadas",
                                        values = uiState.comparativaAsistencia.map { it.third.toDouble() },
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
                atencionesPorMes = mapOf("Jul" to 15, "Ago" to 30, "Sep" to 22, "Oct" to 40),
                pacientesPorEtapa = mapOf("Evaluación" to 10, "Intervención" to 25, "Cierre" to 5),
                comparativaAsistencia = listOf(
                    Triple("Jul", 20, 15),
                    Triple("Ago", 35, 30),
                    Triple("Sep", 30, 22),
                    Triple("Oct", 45, 40),
                    Triple("Nov", 15, 15),
                    Triple("Dic", 5, 2)
                )
            ),
            onVolver = {}
        )
    }
}