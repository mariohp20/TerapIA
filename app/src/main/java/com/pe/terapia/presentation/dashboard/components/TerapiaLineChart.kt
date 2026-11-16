package com.pe.terapia.presentation.dashboard.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line

@Composable
fun TerapiaLineChart(
    data: List<Line>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val colorLineas = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    val colorTexto = MaterialTheme.colorScheme.onSurfaceVariant

    LineChart(
        modifier = modifier,
        data = data,
        gridProperties = GridProperties(
            xAxisProperties = GridProperties.AxisProperties(
                color = SolidColor(colorLineas)
            ),
            yAxisProperties = GridProperties.AxisProperties(
                color = SolidColor(colorLineas)
            )
        ),
        indicatorProperties = HorizontalIndicatorProperties(
            enabled = true,
            textStyle = TextStyle(color = colorTexto)
        ),
        labelProperties = LabelProperties(
            enabled = true,
            labels = labels,
            textStyle = TextStyle(color = colorTexto)
        ),
        labelHelperProperties = LabelHelperProperties(
            textStyle = TextStyle(color = colorTexto)
        )
    )
}