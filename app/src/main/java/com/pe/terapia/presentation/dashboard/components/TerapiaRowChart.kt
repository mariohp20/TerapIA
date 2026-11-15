package com.pe.terapia.presentation.dashboard.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import ir.ehsannarmani.compose_charts.RowChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.VerticalIndicatorProperties

@Composable
fun TerapiaRowChart(
    data: List<Bars>,
    modifier: Modifier = Modifier
) {
    val colorLineas = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
    val colorTexto = MaterialTheme.colorScheme.onSurfaceVariant

    RowChart(
        modifier = modifier,
        data = data,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        barProperties = BarProperties(
            cornerRadius = Bars.Data.Radius.Rectangle(topRight = 6.dp, bottomRight = 6.dp),
            thickness = 24.dp,
            spacing = 2.dp
        ),
        gridProperties = GridProperties(
            xAxisProperties = GridProperties.AxisProperties(
                color = SolidColor(colorLineas),
                thickness = 1.dp
            ),
            yAxisProperties = GridProperties.AxisProperties(
                color = SolidColor(colorLineas),
                thickness = 1.dp
            )
        ),
        indicatorProperties = VerticalIndicatorProperties(
            enabled = true,
            textStyle = TextStyle(color = colorTexto)
        ),
        labelProperties = LabelProperties(
            enabled = true,
            textStyle = TextStyle(color = colorTexto)
        ),
        labelHelperProperties = LabelHelperProperties(
            textStyle = TextStyle(color = colorTexto)
        )
    )
}