package com.pe.terapia.presentation.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.Pie

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TerapiaPieChart(
    datos: Map<String, Int>,
    modifier: Modifier = Modifier,
    mostrarLeyenda: Boolean = true
) {
    val colores = obtenerPaletaGraficos()

    val colorSeleccionado = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)


    val pieData = remember(datos, colores) {
        datos.entries.mapIndexed { index, (etiqueta, cantidad) ->
            Pie(
                label = etiqueta,
                data = cantidad.toDouble(),
                color = colores.getOrElse(index % colores.size) { Color.Gray },
                selectedColor = colorSeleccionado
            )
        }
    }

    Column(modifier = modifier) {
        PieChart(
            modifier = Modifier.fillMaxWidth().weight(1f),
            data = pieData,
            labelHelperProperties = LabelHelperProperties(
                enabled = false
            )
        )

        if (mostrarLeyenda) {
            Spacer(modifier = Modifier.height(16.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                datos.entries.mapIndexed { index, (etiqueta, cantidad) ->
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
                            text = "$etiqueta ($cantidad)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}