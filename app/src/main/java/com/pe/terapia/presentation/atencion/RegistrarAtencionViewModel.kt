package com.pe.terapia.presentation.atencion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.Atencion
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.historiaclinica.RegistrarAtencionUseCase
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.pe.terapia.domain.usecase.historiaclinica.ObtenerAtencionPorCitaUseCase

data class RegistrarAtencionUiState(
    val cargandoInicial: Boolean = true,
    val guardando: Boolean = false,
    val exito: Boolean = false,
    val error: String? = null,
    // Campos del formulario
    val motivoConsulta: String = "",
    val observaciones: String = "",
    val evaluacion: String = "",
    val evolucion: String = "",
    val indicaciones: String = ""
)

class RegistrarAtencionViewModel(
    private val registrarAtencionUseCase: RegistrarAtencionUseCase,
    private val obtenerAtencionPorCitaUseCase: ObtenerAtencionPorCitaUseCase,
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrarAtencionUiState())
    val uiState: StateFlow<RegistrarAtencionUiState> = _uiState.asStateFlow()

    // Funciones para actualizar los campos del formulario
    fun onMotivoConsultaChange(valor: String) { _uiState.value = _uiState.value.copy(motivoConsulta = valor) }
    fun onObservacionesChange(valor: String) { _uiState.value = _uiState.value.copy(observaciones = valor) }
    fun onEvaluacionChange(valor: String) { _uiState.value = _uiState.value.copy(evaluacion = valor) }
    fun onEvolucionChange(valor: String) { _uiState.value = _uiState.value.copy(evolucion = valor) }
    fun onIndicacionesChange(valor: String) { _uiState.value = _uiState.value.copy(indicaciones = valor) }
    fun cargarDatosPrevios(citaId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargandoInicial = true)

            val result = obtenerAtencionPorCitaUseCase(citaId)

            result.onSuccess { atencionPrevia ->
                if (atencionPrevia != null) {
                    _uiState.value = _uiState.value.copy(
                        motivoConsulta = atencionPrevia.motivoConsulta,
                        observaciones = atencionPrevia.observaciones,
                        evaluacion = atencionPrevia.evaluacion,
                        evolucion = atencionPrevia.evolucion,
                        indicaciones = atencionPrevia.indicaciones
                    )
                }
            }

            _uiState.value = _uiState.value.copy(cargandoInicial = false)
        }
    }

    fun guardarAtencion(citaId: String, pacienteId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true, error = null)

            val usuarioActual = obtenerUsuarioActualUseCase().getOrNull()
            if (usuarioActual == null) {
                _uiState.value = _uiState.value.copy(guardando = false, error = "Sesión inválida")
                return@launch
            }

            val estadoActual = _uiState.value
            val nuevaAtencion = Atencion(
                citaId = citaId,
                pacienteId = pacienteId,
                psicologoId = usuarioActual.uid,
                historiaClinicaId = "",
                fechaAtencion = Instant.now(),
                motivoConsulta = estadoActual.motivoConsulta,
                observaciones = estadoActual.observaciones,
                evaluacion = estadoActual.evaluacion,
                evolucion = estadoActual.evolucion,
                indicaciones = estadoActual.indicaciones
            )

            val result = registrarAtencionUseCase(nuevaAtencion)

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(guardando = false, exito = true)
                },
                onFailure = { excepcion ->
                    _uiState.value = _uiState.value.copy(guardando = false, error = excepcion.message)
                }
            )
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}