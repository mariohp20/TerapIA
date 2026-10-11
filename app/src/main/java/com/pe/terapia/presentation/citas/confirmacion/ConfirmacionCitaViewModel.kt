package com.pe.terapia.presentation.citas.confirmacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.Reserva
import com.pe.terapia.domain.model.Rol
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.citas.ReservarCitaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ConfirmacionCitaUiState {
    data object Idle : ConfirmacionCitaUiState
    data object Cargando : ConfirmacionCitaUiState
    data class Confirmada(val citaId: String) : ConfirmacionCitaUiState
    data class Error(val mensaje: String) : ConfirmacionCitaUiState
}

data class ResumenCita(
    val psicologoNombre: String,
    val especialidad: String,
    val sede: String,
    val fecha: String,
    val hora: String,
    val sedeDireccion: String = ""
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
    private val inicioEpochMillis: Long,
    private val reservarCitaUseCase: ReservarCitaUseCase,
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ConfirmacionCitaUiState>(ConfirmacionCitaUiState.Idle)
    val uiState: StateFlow<ConfirmacionCitaUiState> = _uiState.asStateFlow()

    val resumen = ResumenCita(psicologoNombre, especialidadNombre, sedeNombre, fecha, hora)

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
                .onSuccess {
                    _uiState.value = ConfirmacionCitaUiState.Confirmada(it.id)
                }
                .onFailure { _uiState.value = ConfirmacionCitaUiState.Error(it.message ?: "No se pudo confirmar la cita.") }
        }
    }
}
