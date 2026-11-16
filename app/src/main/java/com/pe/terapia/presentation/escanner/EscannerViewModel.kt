package com.pe.terapia.presentation.escanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.EstadoCita
import com.pe.terapia.domain.model.Rol
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.citas.ValidarCitaQrUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EscannerUiState {
    data object Idle : EscannerUiState
    data object Cargando : EscannerUiState
    data class CitaEscaneada(
        val citaId: String,
        val pacienteId: String,
        val pacienteNombre: String,
        val fecha: String,
        val hora: String,
        val especialidadId: String,
        val estado: String,
        val puedeRegistrarAtencion: Boolean,
        val mensaje: String
    ) : EscannerUiState
    data class Error(val mensaje: String) : EscannerUiState
}

class EscannerViewModel(
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase,
    private val validarCitaQrUseCase: ValidarCitaQrUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<EscannerUiState>(EscannerUiState.Idle)
    val uiState: StateFlow<EscannerUiState> = _uiState.asStateFlow()

    fun procesarCodigoScaneado(codigoCita: String) {
        if (codigoCita.isBlank()) {
            _uiState.value = EscannerUiState.Error("Código QR vacío o inválido.")
            return
        }

        viewModelScope.launch {
            _uiState.value = EscannerUiState.Cargando
            try {
                val psicologo = obtenerUsuarioActualUseCase().getOrThrow()
                    ?: error("No hay un psicólogo autenticado.")
                check(psicologo.rol == Rol.PSICOLOGO) {
                    "Esta función solo está disponible para psicólogos."
                }
                val cita = validarCitaQrUseCase(codigoCita.trim(), psicologo.uid).getOrThrow()
                val estado = cita.estado
                val puedeRegistrar = estado == EstadoCita.PROGRAMADA.name &&
                    cita.inicioEpochMillis > 0L && System.currentTimeMillis() >= cita.inicioEpochMillis
                val mensaje = when (estado) {
                    EstadoCita.PROGRAMADA.name -> if (puedeRegistrar) {
                        "Cita validada. Puedes abrir el registro de atención; al guardarlo, la cita quedará completada."
                    } else {
                        "Cita validada. El registro de atención estará disponible desde la hora programada."
                    }
                    EstadoCita.COMPLETADA.name -> "Esta cita ya tiene una atención registrada. No se puede completar otra vez."
                    EstadoCita.NO_ASISTIO.name -> "La cita ya fue marcada como inasistencia."
                    EstadoCita.CANCELADA.name -> "La cita está cancelada y no admite registro de asistencia."
                    else -> "Estado de cita no reconocido; no se puede registrar asistencia."
                }

                _uiState.value = EscannerUiState.CitaEscaneada(
                    citaId = cita.citaId,
                    pacienteId = cita.pacienteId,
                    pacienteNombre = cita.pacienteNombre,
                    fecha = cita.fecha,
                    hora = cita.hora,
                    especialidadId = cita.especialidadId,
                    estado = estado,
                    puedeRegistrarAtencion = puedeRegistrar,
                    mensaje = mensaje
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = EscannerUiState.Error(e.message ?: "Error al verificar la cita.")
            }
        }
    }

    fun reiniciar() {
        _uiState.value = EscannerUiState.Idle
    }
}
