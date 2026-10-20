package com.pe.terapia.presentation.etapas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.EtapaClinica
import com.pe.terapia.domain.model.EstadoEtapa
import com.pe.terapia.domain.model.TipoEtapa
import com.pe.terapia.domain.usecase.historiaclinica.ObtenerTiposEtapasUseCase
import com.pe.terapia.domain.usecase.historiaclinica.ObtenerEtapasPorProcesoUseCase
import com.pe.terapia.domain.usecase.historiaclinica.TransicionarEtapaUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GestionEtapasUiState(
    val cargando: Boolean = true,
    val guardando: Boolean = false,
    val etapasHistorial: List<EtapaClinica> = emptyList(),
    val tiposEtapasCatalogo: List<TipoEtapa> = emptyList(),
    val etapaEnProgreso: EtapaClinica? = null,
    val error: String? = null
)

class GestionEtapasViewModel(
    private val obtenerEtapasPorProcesoUseCase: ObtenerEtapasPorProcesoUseCase,
    private val obtenerTiposEtapasUseCase: ObtenerTiposEtapasUseCase,
    private val transicionarEtapaUseCase: TransicionarEtapaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GestionEtapasUiState())
    val uiState: StateFlow<GestionEtapasUiState> = _uiState.asStateFlow()

    fun cargarDatos(procesoId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)

            val etapasDeferred = async { obtenerEtapasPorProcesoUseCase(procesoId) }
            val catalogoDeferred = async { obtenerTiposEtapasUseCase() }

            val etapasResult = etapasDeferred.await()
            val catalogoResult = catalogoDeferred.await()

            if (etapasResult.isFailure || catalogoResult.isFailure) {
                val errorReal = etapasResult.exceptionOrNull()?.message
                    ?: catalogoResult.exceptionOrNull()?.message
                    ?: "Error desconocido"

                android.util.Log.e("EtapasDebug", "Fallo al cargar: $errorReal")

                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = "Detalle del error: $errorReal"
                )
                return@launch
            }

            val historial = etapasResult.getOrDefault(emptyList()).sortedByDescending { it.fechaInicio }
            val activa = historial.firstOrNull { it.estado == EstadoEtapa.EN_PROGRESO }

            _uiState.value = _uiState.value.copy(
                cargando = false,
                etapasHistorial = historial,
                tiposEtapasCatalogo = catalogoResult.getOrDefault(emptyList()),
                etapaEnProgreso = activa
            )
        }
    }

    fun avanzarEtapa(procesoId: String, nuevoTipoEtapaId: String, observaciones: String) {
        val etapaActualId = _uiState.value.etapaEnProgreso?.id ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true)

            val result = transicionarEtapaUseCase(procesoId, etapaActualId, nuevoTipoEtapaId, observaciones)

            result.onSuccess {
                _uiState.value = _uiState.value.copy(guardando = false)
                cargarDatos(procesoId)
            }.onFailure {
                _uiState.value = _uiState.value.copy(guardando = false, error = it.message)
            }
        }
    }
}