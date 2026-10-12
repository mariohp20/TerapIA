package com.pe.terapia.presentation.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.Atencion
import com.pe.terapia.domain.model.Rol
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.historiaclinica.ObtenerAtencionesPacienteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistorialClinicoUiState(
    val cargando: Boolean = true,
    val atenciones: List<Atencion> = emptyList(),
    val faseActual: String = "Evaluación y orientación",
    val esPsicologo: Boolean = false,
    val error: String? = null
)

class HistorialClinicoViewModel(
    private val obtenerAtencionesUseCase: ObtenerAtencionesPacienteUseCase,
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialClinicoUiState())
    val uiState: StateFlow<HistorialClinicoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val usuario = obtenerUsuarioActualUseCase().getOrNull()
            _uiState.value = _uiState.value.copy(
                esPsicologo = usuario?.rol == Rol.PSICOLOGO
            )
        }
    }

    fun cargarHistorial(pacienteId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)

            obtenerAtencionesUseCase(pacienteId).fold(
                onSuccess = { lista ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        atenciones = lista
                    )
                },
                onFailure = { excepcion ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = excepcion.message
                    )
                }
            )
        }
    }

    fun cambiarFase(nuevaFase: String) {
        _uiState.value = _uiState.value.copy(faseActual = nuevaFase)
    }
}