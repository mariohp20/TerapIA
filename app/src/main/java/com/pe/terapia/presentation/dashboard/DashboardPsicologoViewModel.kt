package com.pe.terapia.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.dashboard.ObtenerAtencionesPsicologoUseCase
import com.pe.terapia.domain.usecase.dashboard.ObtenerEtapasActivasPsicologoUseCase
import com.pe.terapia.domain.usecase.historiaclinica.ObtenerTiposEtapasUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DashboardPsicologoUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val atencionesPorMes: Map<String, Int> = emptyMap(),
    val pacientesPorEtapa: Map<String, Int> = emptyMap()
)

class DashboardPsicologoViewModel(
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase,
    private val obtenerAtencionesPsicologoUseCase: ObtenerAtencionesPsicologoUseCase,
    private val obtenerEtapasActivasPsicologoUseCase: ObtenerEtapasActivasPsicologoUseCase,
    private val obtenerTiposEtapasUseCase: ObtenerTiposEtapasUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardPsicologoUiState())
    val uiState: StateFlow<DashboardPsicologoUiState> = _uiState.asStateFlow()

    init {
        cargarDashboard()
    }

    fun cargarDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)

            val usuario = obtenerUsuarioActualUseCase().getOrNull()
            if (usuario == null) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = "No se pudo identificar al usuario."
                )
                return@launch
            }

            val psicologoId = usuario.uid

            val atenciones = obtenerAtencionesPsicologoUseCase(psicologoId).getOrNull() ?: emptyList()
            val etapas = obtenerEtapasActivasPsicologoUseCase(psicologoId).getOrNull() ?: emptyList()
            val tiposEtapas = obtenerTiposEtapasUseCase().getOrNull() ?: emptyList()

            val mapaNombresEtapas = tiposEtapas.associate { it.id to it.nombre }

            val agrupadoPorEtapa = etapas
                .groupBy { etapa ->
                    mapaNombresEtapas[etapa.tipoEtapaId] ?: "Desconocido"
                }
                .mapValues { it.value.size }

            val formateadorMes = DateTimeFormatter.ofPattern("MMM", Locale("es", "ES"))
            val agrupadoPorMes = atenciones
               .sortedBy { it.fechaAtencion }
                .groupBy { atencion ->
                    val fechaLocal = atencion.fechaAtencion.atZone(ZoneId.systemDefault()).toLocalDate()
                    fechaLocal.format(formateadorMes).replaceFirstChar { it.uppercase() }
                }
                .mapValues { it.value.size }

            _uiState.value = _uiState.value.copy(
                cargando = false,
                pacientesPorEtapa = agrupadoPorEtapa,
                atencionesPorMes = agrupadoPorMes
            )
        }
    }
}