package com.pe.terapia.presentation.home.psicologo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pe.terapia.R
import com.pe.terapia.domain.model.CitaPacienteDetalle
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomePsicologoScreen(
    onGestionarPagos: () -> Unit,
    onCerrarSesion: () -> Unit,
    onRegistrarAtencion: (citaId: String, pacienteId: String) -> Unit,
    onIrAHistorialClinico: () -> Unit,
    viewModel: HomePsicologoViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onIrAHistorialClinico,
                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(R.string.historiales)) },
                text = { Text(stringResource(R.string.historiales_clinicos)) },
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
            // CABECERA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(top = 32.dp, bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (uiState.nombrePsicologo.isNotBlank()) "Hola, Dr. ${uiState.nombrePsicologo}"
                                    else "Hola, Doctor",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.home_psicologo_titulo),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                        )
                    }
                    IconButton(
                        onClick = onCerrarSesion,
                        modifier = Modifier.background(Color.White.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = stringResource(R.string.home_cerrar_sesion), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            // TARJETA BLANCA PRINCIPAL
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                if (uiState.cargando) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp)
                    ) {
                        uiState.error?.let {
                            Text(it, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Boton para gestionar pagos
                        Card(
                            onClick = onGestionarPagos,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text("Gestionar Pagos en Clínica", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text("Confirmar pago físico y emitir comprobante", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Resumen de hoy
                        Text(stringResource(R.string.resumen_de_hoy), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ResumenCard(titulo = stringResource(R.string.citas_hoy), valor = uiState.citasHoy.toString())
                            ResumenCard(titulo = stringResource(R.string.pendientes), valor = uiState.pendientes.toString())
                            ResumenCard(titulo = stringResource(R.string.pacientes_activos), valor = uiState.pacientesActivos.toString())
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Proximas citas
                        Text(stringResource(R.string.home_psicologo_proximas), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.height(16.dp))

                        if (uiState.proximasCitas.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stringResource(R.string.home_psicologo_sin_citas), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            uiState.proximasCitas.forEach { cita ->
                                CitaItemCard(
                                    cita = cita,
                                    onClick = { onRegistrarAtencion(cita.citaId, cita.pacienteId) }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun ResumenCard(titulo: String, valor: String) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = valor, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = titulo, fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, lineHeight = 14.sp)
        }
    }
}

@Composable
fun CitaItemCard(cita: CitaPacienteDetalle, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier
                .width(4.dp)
                .height(24.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50)))
            Spacer(modifier = Modifier.width(12.dp))

            Text(text = cita.hora, fontWeight = FontWeight.Medium, color = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "·", color = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = cita.nombrePaciente, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)

            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Ir", modifier = Modifier.size(16.dp), tint = Color.Gray)
        }
    }
}
