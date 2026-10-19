package com.pe.terapia.presentation.home.paciente.inicio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pe.terapia.domain.model.EstadoCita
import com.pe.terapia.domain.model.EstadoPago
import com.pe.terapia.domain.model.ProximaCita
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun ProximaCitaCard(cita: ProximaCita, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val parsedDate = try { LocalDate.parse(cita.fecha) } catch (_: Exception) { LocalDate.now() }
    val formatterDiaSemana = DateTimeFormatter.ofPattern("EEE", Locale.forLanguageTag("es"))
    val formatterMes = DateTimeFormatter.ofPattern("MMM", Locale.forLanguageTag("es"))

    val diaSemana = parsedDate.format(formatterDiaSemana).replaceFirstChar { it.uppercase() }
    val diaNumero = parsedDate.dayOfMonth.toString()
    val mes = parsedDate.format(formatterMes).replaceFirstChar { it.uppercase() }.removeSuffix(".")

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Bloque de Fecha
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(diaSemana, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(diaNumero, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(mes, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(16.dp))
            // Detalles
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(cita.hora, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    EstadoCitaChip(cita)
                }
                Text("Ps. ${cita.psicologoNombre.split(" ").take(2).joinToString(" ")}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(cita.especialidadNombre, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Ver detalle", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun EstadoCitaChip(cita: ProximaCita, modifier: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme
    val (texto, fondo, contenido) = when {
        cita.estado == EstadoCita.CANCELADA.name -> Triple("Cancelada", c.errorContainer, c.onErrorContainer)
        cita.estado == EstadoCita.COMPLETADA.name -> Triple("Atendida", c.surfaceVariant, c.onSurfaceVariant)
        cita.estadoPago == EstadoPago.PAGADO.name -> Triple("Confirmada", c.primaryContainer, c.onPrimaryContainer)
        else -> Triple("Reservada", c.tertiaryContainer, c.onTertiaryContainer)
    }
    Box(modifier.background(fondo, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
        Text(texto, style = MaterialTheme.typography.labelSmall, color = contenido, fontWeight = FontWeight.SemiBold)
    }
}