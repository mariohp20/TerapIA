package com.pe.terapia.presentation.salaconsulta

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaConsultaScreen(
    citaId: String,
    pacienteId: String,
    pacienteNombre: String,
    onVolver: () -> Unit,
    onNavegarAtencion: (String, String) -> Unit,
    onNavegarHistorial: (String, String) -> Unit,
    onNavegarEtapas: (String) -> Unit,
    viewModel: SalaConsultaViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(pacienteId) {
        viewModel.cargarContexto(pacienteId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sala de Consulta", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // IDENTIFICACIÓN DEL PACIENTE Y FASE ACTUAL
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(pacienteNombre, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Fase del Tratamiento (${uiState.etapaOrden}/${uiState.totalEtapas})", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(uiState.etapaActualNombre, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)

                    // Barra de progreso visual
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { uiState.etapaOrden.toFloat() / uiState.totalEtapas.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("¿Qué deseas hacer hoy?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))

            // ACCIÓN PRINCIPAL: Registrar / Editar la atención de hoy
            AccionCard(
                titulo = "Registrar Atención",
                descripcion = "Tomar notas, evolución e indicaciones de la sesión actual.",
                icono = Icons.Default.NoteAdd,
                colorFondo = MaterialTheme.colorScheme.primary,
                colorIcono = Color.White,
                onClick = { onNavegarAtencion(citaId, pacienteId) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ACCIÓN SECUNDARIA: Ver el expediente previo
            AccionCard(
                titulo = "Ver Historial Clínico",
                descripcion = "Revisar sesiones pasadas, resúmenes y notas privadas.",
                icono = Icons.Default.FolderShared,
                colorFondo = MaterialTheme.colorScheme.surfaceVariant,
                colorIcono = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = { onNavegarHistorial(pacienteId, pacienteNombre) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ACCIÓN SECUNDARIA: Gestionar avance
            AccionCard(
                titulo = "Gestionar Etapas",
                descripcion = "Ver línea de tiempo o avanzar a la siguiente fase clínica.",
                icono = Icons.Default.Timeline,
                colorFondo = MaterialTheme.colorScheme.surfaceVariant,
                colorIcono = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = {
                    if (uiState.procesoId != null) {
                        onNavegarEtapas(uiState.procesoId!!)
                    } else {
                        Toast.makeText(
                            context,
                            "Registre una atención primero para iniciar el Proceso Terapéutico",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
        }
    }
}

@Composable
fun AccionCard(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    colorFondo: Color,
    colorIcono: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = colorFondo),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = colorIcono)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (colorFondo == MaterialTheme.colorScheme.primary) Color.White else MaterialTheme.colorScheme.onSurface)
                Text(descripcion, fontSize = 12.sp, color = if (colorFondo == MaterialTheme.colorScheme.primary) Color.White.copy(alpha = 0.8f) else Color.Gray)
            }
        }
    }
}