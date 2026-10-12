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

data class RegistrarAtencionUiState(
    val guardando: Boolean = false,
    val exito: Boolean = false,
    val error: String? = null
)

class RegistrarAtencionViewModel(
    private val registrarAtencionUseCase: RegistrarAtencionUseCase,
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrarAtencionUiState())
    val uiState: StateFlow<RegistrarAtencionUiState> = _uiState.asStateFlow()

    // Variables de estado para los campos del formulario
    var motivoConsulta = MutableStateFlow("")
    var observaciones = MutableStateFlow("")
    var evaluacion = MutableStateFlow("")
    var evolucion = MutableStateFlow("")
    var indicaciones = MutableStateFlow("")

    fun guardarAtencion(citaId: String, pacienteId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true, error = null)

            val usuarioActual = obtenerUsuarioActualUseCase().getOrNull()
            if (usuarioActual == null) {
                _uiState.value = _uiState.value.copy(guardando = false, error = "Sesión inválida")
                return@launch
            }

            val nuevaAtencion = Atencion(
                citaId = citaId,
                pacienteId = pacienteId,
                psicologoId = usuarioActual.uid,
                historiaClinicaId = "", // Se generará o buscará en el DataSource
                fechaAtencion = Instant.now(),
                motivoConsulta = motivoConsulta.value,
                observaciones = observaciones.value,
                evaluacion = evaluacion.value,
                evolucion = evolucion.value,
                indicaciones = indicaciones.value
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