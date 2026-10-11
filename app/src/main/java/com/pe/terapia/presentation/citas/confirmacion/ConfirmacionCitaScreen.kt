package com.pe.terapia.presentation.citas.confirmacion

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pe.terapia.R
import com.pe.terapia.ui.theme.TerapiaTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ConfirmacionCitaScreen(
    psicologoId: String,
    psicologoNombre: String,
    especialidadId: String,
    especialidadNombre: String,
    sedeId: String,
    sedeNombre: String,
    sedeDireccion: String,
    fecha: String,
    hora: String,
    horarioId: String,
    bloqueId: String,
    inicioEpochMillis: Long,
    onConfirmada: () -> Unit,
    onVolver: () -> Unit,
    viewModel: ConfirmacionCitaViewModel = koinViewModel(
        parameters = { parametersOf(psicologoId, psicologoNombre, especialidadId, especialidadNombre, sedeId, sedeNombre, fecha, hora, horarioId, bloqueId, inicioEpochMillis) }
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    ConfirmacionCitaContent(
        resumen = viewModel.resumen.copy(sedeDireccion = sedeDireccion),
        uiState = uiState,
        onConfirmar = viewModel::confirmar,
        onVolver = onVolver,
        onFinalizar = onConfirmada
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmacionCitaContent(
    resumen: ResumenCita,
    uiState: ConfirmacionCitaUiState,
    onConfirmar: () -> Unit,
    onVolver: () -> Unit,
    onFinalizar: () -> Unit
) {
    val colorFondoTurquesaClaro = MaterialTheme.colorScheme.primaryContainer
    val isConfirmada = uiState is ConfirmacionCitaUiState.Confirmada

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    if (!isConfirmada) {
                        IconButton(onClick = onVolver) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorFondoTurquesaClaro)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .navigationBarsPadding(),
                color = colorFondoTurquesaClaro
            ) {
                Button(
                    onClick = { if (isConfirmada) onFinalizar() else onConfirmar() },
                    enabled = uiState !is ConfirmacionCitaUiState.Cargando,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    if (uiState is ConfirmacionCitaUiState.Cargando) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(
                            text = if (isConfirmada) "Ir a Mis Citas" else stringResource(R.string.citas_confirmar_boton),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorFondoTurquesaClaro)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = isConfirmada,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                },
                label = "TicketTransition"
            ) { confirmado ->

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (confirmado) {
                        Spacer(Modifier.height(16.dp))
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Éxito",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "¡Cita Confirmada!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Tu comprobante de cita",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.height(32.dp))
                    } else {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Resumen de cita",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Revisa los datos antes de continuar",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.height(32.dp))
                    }

                    // TICKET DIGITAL
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp, pressedElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {

                            // Doctor Info
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(colorFondoTurquesaClaro, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = resumen.psicologoNombre.split(" ").filter(String::isNotBlank).take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString(""),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(text = resumen.psicologoNombre, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                    Text(text = resumen.especialidad, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                    Text(text = resumen.sede, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Spacer(Modifier.height(24.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(Modifier.height(24.dp))

                            // Date Info
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = resumen.fecha.toFechaLegible(), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Spacer(Modifier.height(16.dp))

                            // Time Info
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = resumen.hora, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Spacer(Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Outlined.LocationOn, contentDescription = "Ubicación", tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = resumen.sede, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                                    if (resumen.sedeDireccion.isNotBlank()) {
                                        Text(text = resumen.sedeDireccion, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            // Si está confirmada, mostramos el código de cita
                            if (confirmado && uiState is ConfirmacionCitaUiState.Confirmada) {
                                Spacer(Modifier.height(24.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(Modifier.height(24.dp))

                                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Código de cita",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = uiState.citaId,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 2.sp
                                    )
                                }
                            }
                        }
                    }

                    // Manejo de Error
                    if (uiState is ConfirmacionCitaUiState.Error) {
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = uiState.mensaje,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

// PREVIEWS
@Preview(showBackground = true)
@Composable
private fun ConfirmacionCitaPreviewIdle() {
    TerapiaTheme {
        ConfirmacionCitaContent(
            resumen = ResumenCita("Dra. Ana Pérez", "Psicología Clínica", "Sede principal", "2026-10-15", "10:00", "Av. Lima 123, Miraflores, Lima"),
            uiState = ConfirmacionCitaUiState.Idle,
            onConfirmar = {},
            onVolver = {},
            onFinalizar = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfirmacionCitaPreviewConfirmada() {
    TerapiaTheme {
        ConfirmacionCitaContent(
            resumen = ResumenCita("Dra. Ana Pérez", "Psicología Clínica", "Sede principal", "2026-10-15", "10:00", "Av. Lima 123, Miraflores, Lima"),
            uiState = ConfirmacionCitaUiState.Confirmada("TER-2026-B0492"),
            onConfirmar = {},
            onVolver = {},
            onFinalizar = {}
        )
    }
}

private fun String.toFechaLegible(): String = runCatching {
    java.time.LocalDate.parse(this).format(java.time.format.DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", java.util.Locale.forLanguageTag("es-PE")))
        .replaceFirstChar { it.uppercase() }
}.getOrDefault(this)
