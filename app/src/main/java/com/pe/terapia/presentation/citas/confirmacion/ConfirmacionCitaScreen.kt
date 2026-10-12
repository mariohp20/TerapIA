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
    onPagarOnline: (String) -> Unit,
    onPagarEnClinica: (String) -> Unit,
    onFinalizar: () -> Unit,
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
        onPagarOnline = onPagarOnline,
        onPagarEnClinica = { pagoId ->
            viewModel.seleccionarPagoEnClinica(pagoId) {
                onPagarEnClinica(pagoId)
            }
        },
        onVolver = onVolver,
        onFinalizar = onFinalizar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmacionCitaContent(
    resumen: ResumenCita,
    uiState: ConfirmacionCitaUiState,
    onConfirmar: () -> Unit,
    onPagarOnline: (String) -> Unit,
    onPagarEnClinica: (String) -> Unit,
    onVolver: () -> Unit,
    onFinalizar: () -> Unit
) {
    val colorFondoTurquesaClaro = MaterialTheme.colorScheme.primaryContainer
    val isConfirmada = uiState is ConfirmacionCitaUiState.Confirmada
    val pagoId = (uiState as? ConfirmacionCitaUiState.Confirmada)?.pagoId.orEmpty()

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
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isConfirmada) {
                        Button(
                            onClick = { onPagarOnline(pagoId) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(text = "Pagar Online con Mercado Pago (S/ 50.00)", style = MaterialTheme.typography.titleMedium)
                        }

                        OutlinedButton(
                            onClick = { onPagarEnClinica(pagoId) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                        ) {
                            Text(text = "Pagar Físicamente en Clínica (Efectivo)", style = MaterialTheme.typography.titleMedium)
                        }

                        TextButton(
                            onClick = onFinalizar,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Pagar más tarde / Ir al inicio", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        }
                    } else {
                        Button(
                            onClick = onConfirmar,
                            enabled = uiState !is ConfirmacionCitaUiState.Cargando,
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            if (uiState is ConfirmacionCitaUiState.Cargando) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text(
                                    text = stringResource(R.string.citas_confirmar_boton),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
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
                        Spacer(Modifier.height(8.dp))
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Éxito",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "¡Cita Confirmada!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Selecciona tu método de pago",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.height(20.dp))
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

                            Spacer(Modifier.height(20.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(Modifier.height(20.dp))

                            // Date Info
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = resumen.fecha.toFechaLegible(), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Spacer(Modifier.height(14.dp))

                            // Time Info
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = resumen.hora, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Spacer(Modifier.height(14.dp))
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
                                Spacer(Modifier.height(20.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(Modifier.height(20.dp))

                                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Código de cita",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = (uiState as ConfirmacionCitaUiState.Confirmada).citaId,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    }

                    // Manejo de Error
                    if (uiState is ConfirmacionCitaUiState.Error) {
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = (uiState as ConfirmacionCitaUiState.Error).mensaje,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

private fun String.toFechaLegible(): String = runCatching {
    java.time.LocalDate.parse(this).format(java.time.format.DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", java.util.Locale.forLanguageTag("es-PE")))
        .replaceFirstChar { it.uppercase() }
}.getOrDefault(this)
