package com.pe.terapia.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.ComparativaMensualPsicologo
import com.pe.terapia.domain.model.ConteoEtapaPsicologo
import com.pe.terapia.domain.model.ConteoMensualPsicologo
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.dashboard.ObtenerDashboardPsicologoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardPsicologoUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val mesesFiltro: Int = 6,
    val atencionesPorMes: List<ConteoMensualPsicologo> = emptyList(),
    val pacientesPorEtapa: List<ConteoEtapaPsicologo> = emptyList(),
    val comparativaAsistencia: List<ComparativaMensualPsicologo> = emptyList()
)

class DashboardPsicologoViewModel(
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase,
    private val obtenerDashboardPsicologoUseCase: ObtenerDashboardPsicologoUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardPsicologoUiState())
    val uiState: StateFlow<DashboardPsicologoUiState> = _uiState.asStateFlow()

    fun cargarDashboard(meses: Int = _uiState.value.mesesFiltro) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null, mesesFiltro = meses)
            val usuario = obtenerUsuarioActualUseCase().getOrNull()
            if (usuario == null) {
                _uiState.value = DashboardPsicologoUiState(cargando = false, error = "No se pudo identificar al usuario.")
                return@launch
            }
            obtenerDashboardPsicologoUseCase(usuario.uid, meses).fold(
                onSuccess = { data ->
                    _uiState.value = DashboardPsicologoUiState(
                        cargando = false,
                        mesesFiltro = meses,
                        atencionesPorMes = data.atencionesPorMes,
                        pacientesPorEtapa = data.pacientesPorEtapa,
                        comparativaAsistencia = data.comparativaAsistencia
                    )
                },
                onFailure = { error ->
                    _uiState.value = DashboardPsicologoUiState(
                        cargando = false,
                        error = "No se pudieron leer los datos del dashboard: ${error.message ?: "error desconocido"}"
                    )
                }
            )
        }
    }
}
