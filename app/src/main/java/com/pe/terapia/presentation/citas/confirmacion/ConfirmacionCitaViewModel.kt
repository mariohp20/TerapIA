package com.pe.terapia.presentation.citas.confirmacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.Reserva
import com.pe.terapia.domain.model.Rol
import com.pe.terapia.domain.repository.RecordatorioCitasScheduler
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.citas.ReservarCitaUseCase
import com.pe.terapia.domain.usecase.pagos.ObtenerPagoPorCitaUseCase
import com.pe.terapia.domain.usecase.pagos.SeleccionarPagoEnClinicaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ConfirmacionCitaUiState {
    data object Idle : ConfirmacionCitaUiState
    data object Cargando : ConfirmacionCitaUiState
    data class Confirmada(
        val citaId: String,
        val pagoId: String,
        val seleccionandoPagoEnClinica: Boolean = false,
        val errorPagoEnClinica: String? = null
    ) : ConfirmacionCitaUiState
    data class Error(val mensaje: String) : ConfirmacionCitaUiState
}

sealed interface ConfirmacionCitaUiEvent {
    data class IrAPagoEnClinica(val pagoId: String) : ConfirmacionCitaUiEvent
}

data class ResumenCita(
    val psicologoNombre: String,
    val especialidad: String,
    val sede: String,
    val fecha: String,
    val hora: String,
    val sedeDireccion: String = "",
    val costo_cita: Double
)

class ConfirmacionCitaViewModel(
    private val psicologoId: String,
    psicologoNombre: String,
    private val especialidadId: String,
    especialidadNombre: String,
    private val sedeId: String,
    sedeNombre: String,
    private val fecha: String,
    private val hora: String,
    private val horarioId: String,
    private val bloqueId: String,
    private val costo_cita: Double,
    private val inicioEpochMillis: Long,
    private val reservarCitaUseCase: ReservarCitaUseCase,
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase,
    private val obtenerPagoPorCitaUseCase: ObtenerPagoPorCitaUseCase,
    private val seleccionarPagoEnClinicaUseCase: SeleccionarPagoEnClinicaUseCase,
    private val recordatorioCitasScheduler: RecordatorioCitasScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow<ConfirmacionCitaUiState>(ConfirmacionCitaUiState.Idle)
    val uiState: StateFlow<ConfirmacionCitaUiState> = _uiState.asStateFlow()
    private val _uiEvents = MutableSharedFlow<ConfirmacionCitaUiEvent>(extraBufferCapacity = 1)
    val uiEvents: SharedFlow<ConfirmacionCitaUiEvent> = _uiEvents

    val resumen = ResumenCita(psicologoNombre, especialidadNombre, sedeNombre, fecha, hora,"", costo_cita)

    fun confirmar() {
        viewModelScope.launch {
            _uiState.value = ConfirmacionCitaUiState.Cargando

            val resultadoUsuario = obtenerUsuarioActualUseCase()
            val paciente = resultadoUsuario.getOrElse {
                _uiState.value = ConfirmacionCitaUiState.Error(
                    it.message ?: "No se pudo identificar al paciente. Vuelve a iniciar sesión."
                )
                return@launch
            }
            if (paciente == null || paciente.rol != Rol.PACIENTE) {
                _uiState.value = ConfirmacionCitaUiState.Error("No se pudo identificar al paciente. Vuelve a iniciar sesión.")
                return@launch
            }

            val reserva = Reserva(
                pacienteId = paciente.uid,
                psicologoId = psicologoId,
                sedeId = sedeId,
                horarioId = horarioId,
                bloqueId = bloqueId,
                especialidadId = especialidadId,
                fecha = fecha,
                hora = hora,
                inicioEpochMillis = inicioEpochMillis
            )

            reservarCitaUseCase(reserva)
                .onSuccess { cita ->
                    runCatching { recordatorioCitasScheduler.programar(cita.id, cita.inicioEpochMillis, Rol.PACIENTE) }
                    val pago = obtenerPagoPorCitaUseCase(cita.id).getOrNull()
                    _uiState.value = ConfirmacionCitaUiState.Confirmada(
                        citaId = cita.id,
                        pagoId = pago?.id.orEmpty()
                    )
                }
                .onFailure { _uiState.value = ConfirmacionCitaUiState.Error(it.message ?: "No se pudo confirmar la cita.") }
        }
    }

    fun seleccionarPagoEnClinica(pagoId: String) {
        viewModelScope.launch {
            val confirmada = _uiState.value as? ConfirmacionCitaUiState.Confirmada ?: return@launch
            if (confirmada.seleccionandoPagoEnClinica) return@launch
            if (pagoId.isBlank()) {
                _uiState.value = confirmada.copy(errorPagoEnClinica = "No se pudo identificar el pago.")
                return@launch
            }
            _uiState.value = confirmada.copy(seleccionandoPagoEnClinica = true, errorPagoEnClinica = null)
            seleccionarPagoEnClinicaUseCase(pagoId)
                .onSuccess {
                    _uiState.value = confirmada.copy(seleccionandoPagoEnClinica = false)
                    _uiEvents.emit(ConfirmacionCitaUiEvent.IrAPagoEnClinica(pagoId))
                }
                .onFailure {
                    _uiState.value = confirmada.copy(
                        seleccionandoPagoEnClinica = false,
                        errorPagoEnClinica = it.message ?: "No se pudo seleccionar el pago en clínica."
                    )
                }
        }
    }
}
