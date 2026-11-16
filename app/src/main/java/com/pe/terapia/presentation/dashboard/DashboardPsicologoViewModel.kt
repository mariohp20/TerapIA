package com.pe.terapia.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.dashboard.ObtenerDashboardPsicologoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class AtencionMensualPsicologo(
    val etiqueta: String,
    val cantidad: Int
)
data class DistribucionEtapaPsicologo(
    val etapa: String,
    val cantidad: Int
)
data class ComparativaAsistenciaPsicologo(
    val mes: String,
    val programadas: Int,
    val realizadas: Int
)
data class DashboardPsicologoUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val atencionesPorMes: List<AtencionMensualPsicologo> = emptyList(),
    val pacientesPorEtapa: List<DistribucionEtapaPsicologo> = emptyList(),
    val comparativaAsistencia: List<ComparativaAsistenciaPsicologo> = emptyList(),
    val mesesFiltro: Int = 6
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
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    error = "No se pudo identificar al usuario."
                )
                return@launch
            }

            obtenerDashboardPsicologoUseCase(usuario.uid).fold(
                onSuccess = { datos ->
                    val zona = ZoneId.of("America/Lima")
                    val mesActual = YearMonth.now(zona)
                    val formateadorMes = DateTimeFormatter.ofPattern("MMM", Locale("es", "ES"))

                    // PIE CHART: Pacientes por Etapa
                    val mapaNombresEtapas = datos.tiposEtapas.associate { it.id to it.nombre }
                    val agrupadoPorEtapa = datos.etapasActivas
                        .groupBy { mapaNombresEtapas[it.tipoEtapaId] ?: "Desconocido" }
                        .map { (etiqueta, lista) ->
                            DistribucionEtapaPsicologo(etiqueta, lista.size)
                        }

                    val fechasDisponibles = datos.atenciones.map { YearMonth.from(Instant.ofEpochMilli(it.fechaAtencionEpochMillis).atZone(zona)) } +
                            datos.citas.filter { it.inicioEpochMillis > 0 }.map { YearMonth.from(Instant.ofEpochMilli(it.inicioEpochMillis).atZone(zona)) }

                    val primerMesBase = when {
                        meses == 0 -> fechasDisponibles.minOrNull() ?: mesActual
                        else -> mesActual.minusMonths((meses - 1).coerceAtLeast(0).toLong())
                    }

                    val mesesDeDatos = (mesActual.year - primerMesBase.year) * 12 + mesActual.monthValue - primerMesBase.monthValue + 1
                    val primerMes = if (meses == 0 && mesesDeDatos > 60) mesActual.minusMonths(59) else primerMesBase

                    val cantidadMeses = ((mesActual.year - primerMes.year) * 12 + mesActual.monthValue - primerMes.monthValue + 1).coerceIn(1, 60)

                    val mesesGrafico = (0 until cantidadMeses).map { desplazamiento ->
                        primerMes.plusMonths(desplazamiento.toLong())
                    }

                    // COLUMN CHART: Atenciones por Mes
                    val atencionesFiltradas = datos.atenciones.filter { atencion ->
                        YearMonth.from(Instant.ofEpochMilli(atencion.fechaAtencionEpochMillis).atZone(zona)) in primerMes..mesActual
                    }
                    val atencionesPorYearMonth = atencionesFiltradas
                        .groupingBy { YearMonth.from(Instant.ofEpochMilli(it.fechaAtencionEpochMillis).atZone(zona)) }
                        .eachCount()

                    val agrupadoAtenciones = mesesGrafico.map { mes ->
                        AtencionMensualPsicologo(
                            etiqueta = mes.format(formateadorMes).replaceFirstChar { it.uppercase() },
                            cantidad = atencionesPorYearMonth[mes] ?: 0
                        )
                    }

                    // LINE CHART: Comparativa
                    val citasFiltradas = datos.citas.filter { cita ->
                        cita.inicioEpochMillis > 0 && YearMonth.from(Instant.ofEpochMilli(cita.inicioEpochMillis).atZone(zona)) in primerMes..mesActual
                    }
                    val citasPorYearMonth = citasFiltradas
                        .groupingBy { YearMonth.from(Instant.ofEpochMilli(it.inicioEpochMillis).atZone(zona)) }
                        .eachCount()

                    val comparativaAlineada = mesesGrafico.map { mes ->
                        val nombreMes = mes.format(formateadorMes).replaceFirstChar { it.uppercase() }
                        ComparativaAsistenciaPsicologo(
                            mes = nombreMes,
                            programadas = citasPorYearMonth[mes] ?: 0,
                            realizadas = atencionesPorYearMonth[mes] ?: 0
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        pacientesPorEtapa = agrupadoPorEtapa,
                        atencionesPorMes = agrupadoAtenciones,
                        comparativaAsistencia = comparativaAlineada
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = error.message ?: "No se pudo cargar el dashboard. Verifique su conexión."
                    )
                }
            )
        }
    }
}