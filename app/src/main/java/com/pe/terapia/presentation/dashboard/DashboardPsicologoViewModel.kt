package com.pe.terapia.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.dashboard.ObtenerAtencionesPsicologoUseCase
import com.pe.terapia.domain.usecase.dashboard.ObtenerCitasPsicologoUseCase
import com.pe.terapia.domain.usecase.dashboard.ObtenerEtapasActivasPsicologoUseCase
import com.pe.terapia.domain.usecase.historiaclinica.ObtenerTiposEtapasUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DashboardPsicologoUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val atencionesPorMes: Map<String, Int> = emptyMap(),
    val pacientesPorEtapa: Map<String, Int> = emptyMap(),
    val comparativaAsistencia: List<Triple<String, Int, Int>> = emptyList()
)

class DashboardPsicologoViewModel(
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase,
    private val obtenerAtencionesPsicologoUseCase: ObtenerAtencionesPsicologoUseCase,
    private val obtenerEtapasActivasPsicologoUseCase: ObtenerEtapasActivasPsicologoUseCase,
    private val obtenerTiposEtapasUseCase: ObtenerTiposEtapasUseCase,
    private val obtenerCitasPsicologoUseCase: ObtenerCitasPsicologoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardPsicologoUiState())
    val uiState: StateFlow<DashboardPsicologoUiState> = _uiState.asStateFlow()

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

            val atencionesResult = obtenerAtencionesPsicologoUseCase(psicologoId)
            val etapasResult = obtenerEtapasActivasPsicologoUseCase(psicologoId)
            val citasResult = obtenerCitasPsicologoUseCase(psicologoId)
            val tiposEtapasResult = obtenerTiposEtapasUseCase()
            val errorConsulta = listOf(atencionesResult, etapasResult, citasResult, tiposEtapasResult)
                .firstNotNullOfOrNull { it.exceptionOrNull() }
            if (errorConsulta != null) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = "No se pudieron leer los datos del dashboard. Revisa las reglas de Firestore: ${errorConsulta.message ?: "permiso denegado"}"
                )
                return@launch
            }
            val atenciones = atencionesResult.getOrThrow()
            val etapas = etapasResult.getOrThrow()
            val citas = citasResult.getOrThrow()
            val tiposEtapas = tiposEtapasResult.getOrThrow()

            // PieChart
            val mapaNombresEtapas = tiposEtapas.associate { it.id to it.nombre }
            val agrupadoPorEtapa = etapas
                .groupBy { mapaNombresEtapas[it.tipoEtapaId] ?: "Desconocido" }
                .mapValues { it.value.size }

            val formateadorMes = DateTimeFormatter.ofPattern("MMM", Locale("es", "ES"))

            // ColumnChart
            val agrupadoAtenciones = atenciones
                .groupBy {
                    it.fechaAtencion.atZone(ZoneId.systemDefault()).toLocalDate().format(formateadorMes).replaceFirstChar { c -> c.uppercase() }
                }
                .mapValues { it.value.size }

            // LineChart
            val agrupadoCitas = citas
                .groupBy { cita ->
                    Instant.ofEpochMilli(cita.inicioEpochMillis).atZone(ZoneId.systemDefault()).toLocalDate().format(formateadorMes).replaceFirstChar { c -> c.uppercase() }
                }
                .mapValues { it.value.size }

            val ultimos6Meses = (5 downTo 0).map { mesesAtras ->
                LocalDate.now().minusMonths(mesesAtras.toLong())
                    .format(formateadorMes).replaceFirstChar { it.uppercase() }
            }

            val comparativaAlineada = ultimos6Meses.map { mes ->
                Triple(
                    mes,
                    agrupadoCitas[mes] ?: 0,
                    agrupadoAtenciones[mes] ?: 0
                )
            }

            _uiState.value = _uiState.value.copy(
                cargando = false,
                pacientesPorEtapa = agrupadoPorEtapa,
                atencionesPorMes = agrupadoAtenciones,
                comparativaAsistencia = comparativaAlineada
            )
        }
    }
}
