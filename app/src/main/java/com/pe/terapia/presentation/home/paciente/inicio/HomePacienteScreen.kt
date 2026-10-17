package com.pe.terapia.presentation.home.paciente.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List // <--- Nuevo icono importado
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pe.terapia.R
import com.pe.terapia.domain.model.ProximaCita
import com.pe.terapia.ui.theme.TerapiaTheme
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomePacienteScreen(
    onReservarCita: () -> Unit,
    onVerHistorialPagos: () -> Unit,
    onCerrarSesion: () -> Unit,
    onVerHistorial: (String, String) -> Unit,
    mensajeError: String? = null,
    viewModel: HomePacienteViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomePacienteContent(
        uiState = uiState,
        mensajeError = mensajeError,
        onReservarCita = onReservarCita,
        onVerHistorialPagos = onVerHistorialPagos,
        onCerrarSesion = onCerrarSesion,
        onVerHistorial = { viewModel.verHistorial(onVerHistorial) }
    )
}

@Composable
private fun HomePacienteContent(
    uiState: HomePacienteUiState,
    mensajeError: String?,
    onReservarCita: () -> Unit,
    onVerHistorialPagos: () -> Unit,
    onCerrarSesion: () -> Unit,
    onVerHistorial: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onVerHistorial,
                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Mi Historial") },
                text = { Text("Mi Historial Clínico") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Hola, ${uiState.nombrePaciente.ifBlank { "paciente" }}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.home_paciente_titulo),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                    )
                }
                IconButton(
                    onClick = onCerrarSesion,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.65f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = stringResource(R.string.home_cerrar_sesion), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            // Panel blanco inferior
            Surface(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 26.dp)
                ) {
                    (mensajeError ?: uiState.error)?.let { message ->
                        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                    }

                    // 1. Tarjeta de Reserva de Cita
                    Card(
                        onClick = onReservarCita,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 148.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().background(
                                Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))
                            ).padding(horizontal = 22.dp, vertical = 24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.home_nueva_cita), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(6.dp))
                                Text(stringResource(R.string.home_reserva_subtitulo), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f))
                            }
                            Spacer(Modifier.width(16.dp))
                            Box(
                                Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.AddCircleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(38.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // 2. Tarjeta de Pagos
                    Card(
                        onClick = onVerHistorialPagos,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.AutoMirrored.Outlined.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text("Historial de Pagos y Comprobantes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Consulta pagos y estado de tus citas", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(26.dp))
                    Text(stringResource(R.string.home_proxima_cita), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(14.dp))

                    when {
                        uiState.cargando -> Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        uiState.proximaCita != null -> ProximaCitaCard(uiState.proximaCita)
                        else -> Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                Spacer(Modifier.height(10.dp))
                                Text(stringResource(R.string.home_sin_citas), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun ProximaCitaCard(cita: ProximaCita) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(54.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Text(
                        cita.psicologoNombre.split(" ").filter(String::isNotBlank).take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString(""),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(cita.psicologoNombre, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    Text(cita.especialidadNombre, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(16.dp))
            CitaDetailRow(Icons.Outlined.CalendarMonth, cita.fecha.toFechaLegible())
            Spacer(Modifier.height(12.dp))
            CitaDetailRow(Icons.Outlined.Schedule, cita.hora)
            Spacer(Modifier.height(12.dp))
            CitaDetailRow(Icons.Outlined.LocationOn, listOf(cita.sedeNombre, cita.sedeDireccion).filter(String::isNotBlank).joinToString(" · "))
        }
    }
}

@Composable
private fun CitaDetailRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(21.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun String.toFechaLegible(): String = runCatching {
    LocalDate.parse(this).format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-PE")))
        .replaceFirstChar { it.uppercase() }
}.getOrDefault(this)

@Preview(showBackground = true)
@Composable
private fun HomePacientePreview() {
    TerapiaTheme {
        HomePacienteContent(
            uiState = HomePacienteUiState(
                nombrePaciente = "Andrea",
                proximaCita = ProximaCita("Dra. Ana Pérez", "Psicología general", "2026-10-14", "10:00", "Sede principal", "Av. Lima 100, Miraflores, Lima"),
                cargando = false
            ),
            mensajeError = null,
            onReservarCita = {},
            onVerHistorialPagos = {},
            onCerrarSesion = {},
            onVerHistorial = {}
        )
    }
}