package com.pe.terapia.presentation.citas.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.ProximaCita
import com.pe.terapia.domain.model.SlotDisponible
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerCitasPacienteUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerDisponibilidadUseCase
import com.pe.terapia.domain.usecase.citas.SolicitarGestionCitaUseCase
import com.pe.terapia.domain.usecase.citas.TieneSolicitudGestionPendienteUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerSolicitudGestionPendienteUseCase
import com.pe.terapia.domain.usecase.citas.CancelarCitaPacienteUseCase
import com.pe.terapia.domain.repository.RecordatorioCitasScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DetalleCitaUiState {
    data object Cargando : DetalleCitaUiState
    data class Contenido(
        val cita: ProximaCita,
        val pacienteId: String,
        val pacienteNombre: String,
        val enviandoSolicitud: Boolean = false,
        val solicitudEnviada: Boolean = false,
        val solicitudPendiente: com.pe.terapia.domain.model.SolicitudGestionCita? = null,
        val errorSolicitud: String? = null,
        val cancelando: Boolean = false,
        val canceladaPorPaciente: Boolean = false,
        val fechaReprogramacion: String? = null,
        val cargandoHorariosReprogramacion: Boolean = false,
        val horariosReprogramacion: List<SlotDisponible> = emptyList(),
        val slotReprogramacion: SlotDisponible? = null
    ) : DetalleCitaUiState
    data class Error(val mensaje: String) : DetalleCitaUiState
}

class DetalleCitaViewModel(
    private val citaId: String,
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase,
    private val obtenerCitasPacienteUseCase: ObtenerCitasPacienteUseCase,
    private val solicitarGestionCitaUseCase: SolicitarGestionCitaUseCase,
    private val obtenerDisponibilidadUseCase: ObtenerDisponibilidadUseCase,
    private val tieneSolicitudGestionPendienteUseCase: TieneSolicitudGestionPendienteUseCase,
    private val obtenerSolicitudGestionPendienteUseCase: ObtenerSolicitudGestionPendienteUseCase,
    private val cancelarCitaPacienteUseCase: CancelarCitaPacienteUseCase,
    private val recordatorioCitasScheduler: RecordatorioCitasScheduler
) : ViewModel() {
    private val _uiState = MutableStateFlow<DetalleCitaUiState>(DetalleCitaUiState.Cargando)
    val uiState: StateFlow<DetalleCitaUiState> = _uiState.asStateFlow()
    private var cargandoInicial = false

    fun recargar() {
        if (cargandoInicial) return
        val state = _uiState.value
        if (state is DetalleCitaUiState.Contenido && (state.enviandoSolicitud || state.cancelando)) return
        viewModelScope.launch {
            cargarDetalle()
        }
    }

    fun solicitarGestion(tipo: String) {
        val state = _uiState.value as? DetalleCitaUiState.Contenido ?: return
        if (tipo == "REPROGRAMACION") {
            _uiState.value = state.copy(errorSolicitud = "Elige primero una fecha y hora disponibles.")
            return
        }
        if (state.enviandoSolicitud || state.solicitudEnviada) return
        if (state.cita.estado != "PROGRAMADA" || state.cita.inicioEpochMillis <= System.currentTimeMillis()) {
            _uiState.value = state.copy(errorSolicitud = "Solo puedes solicitar cambios para una cita futura programada.")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(enviandoSolicitud = true, errorSolicitud = null)
            solicitarGestionCitaUseCase(citaId, state.pacienteId, tipo).fold(
                onSuccess = { _uiState.value = state.copy(solicitudEnviada = true) },
                onFailure = { _uiState.value = state.copy(errorSolicitud = it.message ?: "No se pudo enviar la solicitud.") }
            )
        }
    }

    fun cargarHorariosReprogramacion(fecha: String) {
        val state = _uiState.value as? DetalleCitaUiState.Contenido ?: return
        if (state.solicitudEnviada || state.cargandoHorariosReprogramacion) return
        val fechaSolicitada = runCatching { java.time.LocalDate.parse(fecha) }.getOrNull()
        if (fechaSolicitada == null) {
            _uiState.value = state.copy(errorSolicitud = "La fecha seleccionada no es válida.")
            return
        }
        if (fechaSolicitada.isBefore(java.time.LocalDate.now(java.time.ZoneId.of("America/Lima")))) {
            _uiState.value = state.copy(
                fechaReprogramacion = fecha,
                horariosReprogramacion = emptyList(),
                slotReprogramacion = null,
                cargandoHorariosReprogramacion = false,
                errorSolicitud = "Elige una fecha futura."
            )
            return
        }
        _uiState.value = state.copy(
            fechaReprogramacion = fecha,
            horariosReprogramacion = emptyList(),
            slotReprogramacion = null,
            cargandoHorariosReprogramacion = true,
            errorSolicitud = null
        )
        viewModelScope.launch {
            obtenerDisponibilidadUseCase.horariosDisponibles(
                state.cita.psicologoId,
                state.cita.sedeId,
                fecha
            ).fold(
                onSuccess = { slots ->
                    val current = _uiState.value as? DetalleCitaUiState.Contenido ?: return@fold
                    if (current.fechaReprogramacion == fecha) {
                        _uiState.value = current.copy(
                            cargandoHorariosReprogramacion = false,
                            horariosReprogramacion = slots.filter { it.inicioEpochMillis != current.cita.inicioEpochMillis }
                        )
                    }
                },
                onFailure = { error ->
                    val current = _uiState.value as? DetalleCitaUiState.Contenido ?: return@fold
                    if (current.fechaReprogramacion == fecha) {
                        _uiState.value = current.copy(
                            cargandoHorariosReprogramacion = false,
                            errorSolicitud = error.message ?: "No se pudo consultar la disponibilidad."
                        )
                    }
                }
            )
        }
    }

    fun seleccionarHorarioReprogramacion(slot: SlotDisponible) {
        val state = _uiState.value as? DetalleCitaUiState.Contenido ?: return
        if (slot in state.horariosReprogramacion) _uiState.value = state.copy(slotReprogramacion = slot, errorSolicitud = null)
    }

    fun solicitarReprogramacion() {
        val state = _uiState.value as? DetalleCitaUiState.Contenido ?: return
        val slot = state.slotReprogramacion ?: run {
            _uiState.value = state.copy(errorSolicitud = "Selecciona una hora disponible.")
            return
        }
        if (slot.inicioEpochMillis <= System.currentTimeMillis()) {
            _uiState.value = state.copy(slotReprogramacion = null, errorSolicitud = "Ese turno ya pasó. Elige otro horario disponible.")
            return
        }
        if (state.enviandoSolicitud || state.solicitudEnviada) return
        viewModelScope.launch {
            _uiState.value = state.copy(enviandoSolicitud = true, errorSolicitud = null)
            solicitarGestionCitaUseCase(citaId, state.pacienteId, "REPROGRAMACION", slot).fold(
                onSuccess = { _uiState.value = state.copy(enviandoSolicitud = false, solicitudEnviada = true) },
                onFailure = { error -> _uiState.value = state.copy(enviandoSolicitud = false, errorSolicitud = error.message ?: "No se pudo enviar la propuesta.") }
            )
        }
    }

    fun cancelarCita() {
        val state = _uiState.value as? DetalleCitaUiState.Contenido ?: return
        if (state.cancelando || state.canceladaPorPaciente) return
        viewModelScope.launch {
            _uiState.value = state.copy(cancelando = true, errorSolicitud = null)
            cancelarCitaPacienteUseCase(citaId, state.pacienteId).fold(
                onSuccess = {
                    recordatorioCitasScheduler.cancelar(citaId)
                    _uiState.value = state.copy(cita = state.cita.copy(estado = "CANCELADA"), canceladaPorPaciente = true, cancelando = false)
                },
                onFailure = { _uiState.value = state.copy(cancelando = false, errorSolicitud = it.message ?: "No se pudo cancelar la cita.") }
            )
        }
    }

    private suspend fun cargarDetalle() {
        cargandoInicial = true
        try {
            val usuario = obtenerUsuarioActualUseCase().getOrNull()
            if (usuario == null) {
                _uiState.value = DetalleCitaUiState.Error("Usuario no identificado.")
                return
            }
            obtenerCitasPacienteUseCase(usuario.uid)
                .onSuccess { lista ->
                    val cita = lista.firstOrNull { it.id == citaId }
                    if (cita == null) {
                        _uiState.value = DetalleCitaUiState.Error("No se encontró la cita.")
                    } else {
                        val solicitudResult = tieneSolicitudGestionPendienteUseCase(citaId, usuario.uid)
                        val pending = obtenerSolicitudGestionPendienteUseCase(citaId, usuario.uid).getOrNull()
                        _uiState.value = DetalleCitaUiState.Contenido(
                            cita = cita,
                            pacienteId = usuario.uid,
                            pacienteNombre = usuario.nombreCompleto,
                            solicitudEnviada = pending != null || solicitudResult.getOrDefault(false),
                            solicitudPendiente = pending,
                            errorSolicitud = solicitudResult.exceptionOrNull()?.message
                        )
                    }
                }
                .onFailure {
                    _uiState.value = DetalleCitaUiState.Error(it.message ?: "No se pudo cargar la cita.")
                }
        } finally {
            cargandoInicial = false
        }
    }

    init { recargar() }
}
