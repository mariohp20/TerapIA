package com.pe.terapia.presentation.salaconsulta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.EstadoEtapa
import com.pe.terapia.domain.model.EstadoProceso
import com.pe.terapia.domain.usecase.historiaclinica.ObtenerTiposEtapasUseCase
import com.pe.terapia.domain.usecase.historiaclinica.ObtenerProcesoActivoUseCase
import com.pe.terapia.domain.usecase.historiaclinica.ObtenerEtapasPorProcesoUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SalaConsultaUiState(
    val cargando: Boolean = true,
    val procesoId: String? = null,
    val etapaActualNombre: String = "Evaluación y orientación",
    val etapaOrden: Int = 1,
    val totalEtapas: Int = 4,
    val error: String? = null
)

class SalaConsultaViewModel(
    private val obtenerProcesoActivoUseCase: ObtenerProcesoActivoUseCase,
    private val obtenerEtapasPorProcesoUseCase: ObtenerEtapasPorProcesoUseCase,
    private val obtenerTiposEtapasUseCase: ObtenerTiposEtapasUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalaConsultaUiState())
    val uiState: StateFlow<SalaConsultaUiState> = _uiState.asStateFlow()

    fun cargarContexto(pacienteId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)

            try {
                // 1. Cargamos los tipos de procesos
                val catalogoResult = obtenerTiposEtapasUseCase().getOrNull() ?: emptyList()
                val total = if (catalogoResult.isNotEmpty()) catalogoResult.size else 4

                // 2. Buscamos si hay un proceso en curso
                val proceso = obtenerProcesoActivoUseCase(pacienteId).getOrNull()

                if (proceso == null) {
                    // Si es un paciente nuevo, la fase es la 1 por defecto
                    val faseInicial = catalogoResult.firstOrNull()
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        procesoId = null,
                        etapaActualNombre = faseInicial?.nombre ?: "Evaluación y orientación",
                        etapaOrden = faseInicial?.orden ?: 1,
                        totalEtapas = total
                    )
                    return@launch
                }

                // 3. Si hay proceso, buscamos la etapa que esté "EN_PROGRESO"
                val etapasResult = obtenerEtapasPorProcesoUseCase(proceso.id).getOrNull() ?: emptyList()
                val etapaActiva = etapasResult.find { it.estado == EstadoEtapa.EN_PROGRESO }

                // 4. Cruzamos el ID de la etapa activa con el catálogo para sacar su nombre
                val tipoEtapa = catalogoResult.find { it.id == etapaActiva?.tipoEtapaId }

                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    procesoId = proceso.id,
                    etapaActualNombre = tipoEtapa?.nombre ?: "Evaluación y orientación",
                    etapaOrden = tipoEtapa?.orden ?: 1,
                    totalEtapas = total
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(cargando = false, error = "Error al cargar el contexto clínico")
            }
        }
    }
}