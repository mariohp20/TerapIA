package com.pe.terapia.presentation.citas.detalle

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pe.terapia.domain.model.EstadoPago
import com.pe.terapia.domain.model.ProximaCita
import com.pe.terapia.presentation.home.paciente.inicio.components.EstadoCitaChip
import com.pe.terapia.presentation.home.paciente.inicio.components.CitaQrDialog
import com.pe.terapia.domain.model.EstadoCita
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleCitaScreen(
    citaId: String,
    onVolver: () -> Unit,
    onVerPagos: () -> Unit,
    onVerHistorial: (String, String, String) -> Unit,
    viewModel: DetalleCitaViewModel = koinViewModel(parameters = { parametersOf(citaId) })
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, citaId) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) viewModel.recargar()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        viewModel.recargar()
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingCoordinates by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            pendingCoordinates?.let { (latitude, longitude) ->
                scope.launch {
                    runCatching {
                        val client = LocationServices.getFusedLocationProviderClient(context)
                        val current = client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token).await()
                            ?: error("No se pudo obtener tu ubicación. Activa el GPS e inténtalo de nuevo.")
                        val destino = android.location.Location("sede").apply { this.latitude = latitude; this.longitude = longitude }
                        val distanciaKm = (current.distanceTo(destino) / 1000.0).let { String.format(java.util.Locale("es", "PE"), "%.1f", it) }
                        val navigation = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$latitude,$longitude"))
                        navigation.setPackage("com.google.android.apps.maps")
                        if (navigation.resolveActivity(context.packageManager) != null) context.startActivity(navigation)
                        else context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")))
                        Toast.makeText(context, "La sede está aproximadamente a $distanciaKm km de ti.", Toast.LENGTH_LONG).show()
                    }.onFailure {
                        Toast.makeText(context, it.message ?: "No se pudo abrir la ruta.", Toast.LENGTH_LONG).show()
                    }
                    pendingCoordinates = null
                }
            }
        } else {
            pendingCoordinates = null
            Toast.makeText(context, "Se necesita permiso de ubicación para calcular la distancia.", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de la cita", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding)) {
            when (val state = uiState) {
                DetalleCitaUiState.Cargando -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is DetalleCitaUiState.Error -> Text(
                    state.mensaje, color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )
                is DetalleCitaUiState.Contenido -> DetalleCitaContenido(
                    cita = state.cita,
                    pacienteId = state.pacienteId,
                    pacienteNombre = state.pacienteNombre,
                    onVerPagos = onVerPagos,
                    onVerHistorial = onVerHistorial,
                    onSolicitarGestion = viewModel::solicitarGestion,
                    enviandoSolicitud = state.enviandoSolicitud,
                    solicitudEnviada = state.solicitudEnviada,
                    solicitudPendiente = state.solicitudPendiente,
                    errorSolicitud = state.errorSolicitud,
                    fechaReprogramacion = state.fechaReprogramacion,
                    cargandoHorariosReprogramacion = state.cargandoHorariosReprogramacion,
                    horariosReprogramacion = state.horariosReprogramacion,
                    slotReprogramacion = state.slotReprogramacion,
                    onCargarHorariosReprogramacion = viewModel::cargarHorariosReprogramacion,
                    onSeleccionarHorarioReprogramacion = viewModel::seleccionarHorarioReprogramacion,
                    onEnviarPropuestaReprogramacion = { viewModel.solicitarReprogramacion() },
                    onCancelarCita = viewModel::cancelarCita,
                    cancelando = state.cancelando,
                    canceladaPorPaciente = state.canceladaPorPaciente,
                    onComoLlegar = { cita ->
                        if (cita.sedeLatitud == 0.0 && cita.sedeLongitud == 0.0) {
                            Toast.makeText(context, "La ubicación de esta sede aún no está configurada.", Toast.LENGTH_LONG).show()
                        } else {
                            pendingCoordinates = cita.sedeLatitud to cita.sedeLongitud
                            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            if (granted) locationPermission.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
                            else locationPermission.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DetalleCitaContenido(
    cita: ProximaCita,
    pacienteId: String,
    pacienteNombre: String,
    onVerPagos: () -> Unit,
    onVerHistorial: (String, String, String) -> Unit,
    onSolicitarGestion: (String) -> Unit,
    enviandoSolicitud: Boolean,
    solicitudEnviada: Boolean,
    solicitudPendiente: com.pe.terapia.domain.model.SolicitudGestionCita?,
    errorSolicitud: String?,
    fechaReprogramacion: String?,
    cargandoHorariosReprogramacion: Boolean,
    horariosReprogramacion: List<com.pe.terapia.domain.model.SlotDisponible>,
    slotReprogramacion: com.pe.terapia.domain.model.SlotDisponible?,
    onCargarHorariosReprogramacion: (String) -> Unit,
    onSeleccionarHorarioReprogramacion: (com.pe.terapia.domain.model.SlotDisponible) -> Unit,
    onEnviarPropuestaReprogramacion: () -> Unit,
    onCancelarCita: () -> Unit,
    cancelando: Boolean,
    canceladaPorPaciente: Boolean,
    onComoLlegar: (ProximaCita) -> Unit
) {
    var mostrarQr by remember(cita.id) { mutableStateOf(false) }
    var mostrarGestion by remember(cita.id) { mutableStateOf(false) }
    var mostrarSelectorReprogramacion by remember(cita.id) { mutableStateOf(false) }
    var mostrarSelectorFecha by remember(cita.id) { mutableStateOf(false) }
    val selectorFechaState = rememberDatePickerState()
    LaunchedEffect(solicitudEnviada) {
        if (solicitudEnviada) mostrarSelectorReprogramacion = false
    }
    val iniciales = cita.psicologoNombre.split(" ").filter(String::isNotBlank).take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TARJETA DEL PROFESIONAL
        Card(
            Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(60.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Text(iniciales, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(cita.psicologoNombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(cita.especialidadNombre, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
                EstadoCitaChip(cita) // Reutilizamos el chip que ya creamos
            }
        }

        // DESCRIPCIÓN
        SeccionCard("Sobre el profesional") {
            Text(cita.psicologoDescripcion.ifBlank { "El profesional aún no tiene una descripción detallada en su perfil." }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // FECHA Y LUGAR
        SeccionCard("Detalles de la atención") {
            InfoFila(Icons.Outlined.CalendarMonth, cita.fecha)
            InfoFila(Icons.Outlined.Schedule, cita.hora)
            InfoFila(Icons.Outlined.LocationOn, listOf(cita.sedeNombre, cita.sedeDireccion).filter(String::isNotBlank).joinToString(" · "))
            if (cita.sedeLatitud != 0.0 || cita.sedeLongitud != 0.0) {
                OutlinedButton(onClick = { onComoLlegar(cita) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cómo llegar y calcular distancia")
                }
            }
        }

        // PAGOS
        SeccionCard("Estado del Pago") {
            Text(
                when (cita.estadoPago) {
                    EstadoPago.PAGADO.name -> "El pago ha sido confirmado."
                    EstadoPago.PENDIENTE.name -> "El pago está pendiente."
                    else -> cita.estadoPago
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            OutlinedButton(onClick = onVerPagos, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("Ver mis pagos y comprobantes")
            }
        }

        if (canceladaPorPaciente || (cita.estado == EstadoCita.PROGRAMADA.name && cita.inicioEpochMillis > System.currentTimeMillis())) {
            SeccionCard("¿Necesitas cambiar esta cita?") {
                Text(
                    if (canceladaPorPaciente) "La cita fue cancelada y el turno quedó disponible nuevamente."
                    else if (solicitudEnviada) {
                        val propuesta = solicitudPendiente?.let { " Propuesta: ${it.fechaSolicitada ?: "—"} a las ${it.horaSolicitada ?: "—"}." }.orEmpty()
                        "Tu solicitud de ${if (solicitudPendiente?.tipo == "CANCELACION") "cancelación" else "reprogramación"} está pendiente.$propuesta La cita actual sigue vigente hasta que la clínica la resuelva."
                    }
                    else "Para reprogramar, propone un horario disponible con el mismo profesional y sede. Tu cita actual sigue vigente hasta que el administrador revise la propuesta. Las cancelaciones pagadas o con menos de 24 horas también requieren revisión; cualquier devolución dependerá de la política de la clínica.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                errorSolicitud?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                if (!solicitudEnviada && !canceladaPorPaciente) {
                    OutlinedButton(onClick = { mostrarGestion = true }, enabled = !enviandoSolicitud && !cancelando, modifier = Modifier.fillMaxWidth()) {
                        if (enviandoSolicitud || cancelando) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Text("Solicitar cancelación o reprogramación")
                    }
                }
            }
        }

        SeccionCard("Código QR de la cita") {
            Text(
                text = when (cita.estado) {
                    EstadoCita.COMPLETADA.name -> "Esta cita figura como atendida. Puedes mostrar el QR para consultar su registro."
                    EstadoCita.CANCELADA.name -> "Esta cita fue cancelada. El QR solo permite consultar su estado; no registra asistencia."
                    EstadoCita.NO_ASISTIO.name -> "Esta cita figura como inasistencia. El QR solo permite consultar su estado."
                    else -> "Muestra este código al psicólogo para validar tu cita al llegar."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(onClick = { mostrarQr = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.QrCode2, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Ver QR de esta cita")
            }
            if (cita.estado == EstadoCita.COMPLETADA.name) {
                TextButton(
                    onClick = { onVerHistorial(pacienteId, pacienteNombre, cita.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver mi historial clínico")
                }
            }
        }
    }

    if (mostrarQr) {
        CitaQrDialog(
            token = cita.id,
            onDismiss = { mostrarQr = false },
            mensaje = if (cita.estado == EstadoCita.PROGRAMADA.name) {
                "Muestra este código al psicólogo para validar la cita."
            } else {
                "Este código consulta la cita y su estado. No cambia ni registra la asistencia."
            }
        )
    }
    if (mostrarGestion) {
        AlertDialog(
            onDismissRequest = { mostrarGestion = false },
            title = { Text("Solicitar un cambio") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val puedeCancelarDirectamente = !cita.pagado && cita.estadoPago != "PAGADO" && System.currentTimeMillis() < cita.inicioEpochMillis - 24L * 60 * 60 * 1000
                    Text("Puedes proponer una fecha y hora disponibles con el mismo profesional y en la misma sede. El administrador volverá a verificar el turno antes de aceptar; mientras tanto, tu cita actual sigue vigente. La cancelación directa está disponible hasta 24 horas antes solo para citas no pagadas. En los demás casos, la clínica revisará tu solicitud y te informará si corresponde una devolución según su política.")
                    if (puedeCancelarDirectamente) {
                        OutlinedButton(onClick = { mostrarGestion = false; onCancelarCita() }, modifier = Modifier.fillMaxWidth()) {
                            Text("Cancelar cita")
                        }
                    } else {
                        OutlinedButton(onClick = { mostrarGestion = false; onSolicitarGestion("CANCELACION") }, modifier = Modifier.fillMaxWidth()) {
                            Text("Solicitar cancelación")
                        }
                    }
                    Button(onClick = { mostrarGestion = false; mostrarSelectorReprogramacion = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Solicitar reprogramación")
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { mostrarGestion = false }) { Text("Volver") } }
        )
    }
    if (mostrarSelectorReprogramacion) {
        AlertDialog(
            onDismissRequest = { if (!enviandoSolicitud) mostrarSelectorReprogramacion = false },
            title = { Text("Propón otro horario") },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Elige un turno libre con ${cita.psicologoNombre} en ${cita.sedeNombre}. La propuesta no reserva el turno; el administrador comprobará que siga libre al responder.")
                    if (fechaReprogramacion != null && !solicitudEnviada) {
                        Text("La ventana seguirá abierta para que elijas un turno. Al enviar la propuesta, se cerrará y tu cita actual seguirá vigente mientras la revisan.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OutlinedButton(onClick = { mostrarSelectorFecha = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(fechaReprogramacion?.let { "Fecha: $it · Cambiar" } ?: "Elegir fecha")
                    }
                    if (cargandoHorariosReprogramacion) {
                        CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                    } else if (fechaReprogramacion != null && horariosReprogramacion.isEmpty()) {
                        Text("No hay turnos disponibles ese día. Prueba otra fecha.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        horariosReprogramacion.forEach { slot ->
                            FilterChip(
                                selected = slotReprogramacion?.inicioEpochMillis == slot.inicioEpochMillis,
                                onClick = { onSeleccionarHorarioReprogramacion(slot) },
                                label = { Text("${slot.hora} · ${slot.duracionMinutos} min") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    slotReprogramacion?.let {
                        Text("Propuesta: ${it.fecha} a las ${it.hora}", color = MaterialTheme.colorScheme.primary)
                    }
                    errorSolicitud?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onEnviarPropuestaReprogramacion,
                    enabled = slotReprogramacion != null && !enviandoSolicitud
                ) {
                    if (enviandoSolicitud) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Text("Enviar propuesta")
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!enviandoSolicitud) mostrarSelectorReprogramacion = false }) { Text("Volver") }
            }
        )
    }
    if (mostrarSelectorFecha) {
        DatePickerDialog(
            onDismissRequest = { mostrarSelectorFecha = false },
            confirmButton = {
                TextButton(onClick = {
                    selectorFechaState.selectedDateMillis?.let { millis ->
                        val fecha = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneOffset.UTC).toLocalDate().toString()
                        onCargarHorariosReprogramacion(fecha)
                    }
                    mostrarSelectorFecha = false
                }) { Text("Ver disponibilidad") }
            },
            dismissButton = { TextButton(onClick = { mostrarSelectorFecha = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = selectorFechaState)
        }
    }
}

@Composable
private fun SeccionCard(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            contenido()
        }
    }
}

@Composable
private fun InfoFila(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
