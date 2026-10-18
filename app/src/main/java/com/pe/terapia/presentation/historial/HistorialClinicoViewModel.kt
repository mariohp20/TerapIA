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
import com.pe.terapia.domain.model.TipoEtapa
import com.pe.terapia.domain.usecase.historiaclinica.ObtenerTiposEtapasUseCase
import kotlinx.coroutines.async

data class HistorialClinicoUiState(
    val cargando: Boolean = true,
    val atenciones: List<Atencion> = emptyList(),
    val tiposEtapas: List<TipoEtapa> = emptyList(),
    val etapaActual: TipoEtapa? = null,
    val esPsicologo: Boolean = false,
    val error: String? = null
)

class HistorialClinicoViewModel(
    private val obtenerAtencionesUseCase: ObtenerAtencionesPacienteUseCase,
    private val obtenerTiposEtapasUseCase: ObtenerTiposEtapasUseCase,
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

            val etapasDeferred = async { obtenerTiposEtapasUseCase() }
            val atencionesDeferred = async { obtenerAtencionesUseCase(pacienteId) }

            val etapasResult = etapasDeferred.await()
            val atencionesResult = atencionesDeferred.await()

            if (atencionesResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = atencionesResult.exceptionOrNull()?.message ?: "Error al cargar el historial"
                )
                return@launch
            }

            val listaAtenciones = atencionesResult.getOrDefault(emptyList())
            val listaEtapas = etapasResult.getOrDefault(emptyList())

            val etapaActiva = listaEtapas.firstOrNull()

            _uiState.value = _uiState.value.copy(
                cargando = false,
                atenciones = listaAtenciones,
                tiposEtapas = listaEtapas,
                etapaActual = etapaActiva
            )
        }
    }

    fun cambiarFase(nuevaEtapa: TipoEtapa) {
        _uiState.value = _uiState.value.copy(etapaActual = nuevaEtapa)
    }
}