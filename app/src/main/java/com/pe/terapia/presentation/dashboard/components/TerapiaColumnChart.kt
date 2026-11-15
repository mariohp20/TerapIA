package com.pe.terapia.presentation.dashboard.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties

@Composable
fun TerapiaColumnChart(
    data: List<Bars>,
    modifier: Modifier = Modifier
) {
    val colorLineas = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
    val colorTexto = MaterialTheme.colorScheme.onSurfaceVariant

    ColumnChart(
        modifier = modifier,
        data = data,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        barProperties = BarProperties(
            cornerRadius = Bars.Data.Radius.Rectangle(topRight = 6.dp, topLeft = 6.dp),
            thickness = 22.dp,
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
        indicatorProperties = HorizontalIndicatorProperties(
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