package com.pe.terapia.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.EstadoCita
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.dashboard.ObtenerDashboardPacienteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ConteoMensualPaciente(
    val periodo: YearMonth,
    val etiqueta: String,
    val cantidad: Int
)

data class EstadoCitaPaciente(
    val etiqueta: String,
    val cantidad: Int
)

data class DashboardPacienteUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val asistenciasPorMes: List<ConteoMensualPaciente> = emptyList(),
    val estadosHistoricos: List<EstadoCitaPaciente> = emptyList(),
    val totalAtenciones: Int = 0,
    val mesesFiltro: Int = 6
)

class DashboardPacienteViewModel(
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase,
    private val obtenerDashboardPacienteUseCase: ObtenerDashboardPacienteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardPacienteUiState())
    val uiState: StateFlow<DashboardPacienteUiState> = _uiState.asStateFlow()

    fun cargarDashboard(meses: Int = _uiState.value.mesesFiltro) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null, mesesFiltro = meses)
            val usuario = obtenerUsuarioActualUseCase().getOrNull()
            if (usuario == null) {
                _uiState.value = DashboardPacienteUiState(
                    cargando = false,
                    error = "No se pudo identificar la sesión del paciente."
                )
                return@launch
            }

            obtenerDashboardPacienteUseCase(usuario.uid).fold(
                onSuccess = { datos ->
                    val zona = ZoneId.of("America/Lima")
                    val mesActual = YearMonth.now(zona)
                    val fechasDisponibles = datos.atenciones.map { YearMonth.from(it.fechaAtencion.atZone(zona)) } +
                        datos.citas.filter { it.inicioEpochMillis > 0 }.map { YearMonth.from(Instant.ofEpochMilli(it.inicioEpochMillis).atZone(zona)) }
                    val primerMesBase = when {
                        meses == 0 -> fechasDisponibles.minOrNull() ?: mesActual
                        else -> mesActual.minusMonths((meses - 1).coerceAtLeast(0).toLong())
                    }
                    val mesesDeDatos = (mesActual.year - primerMesBase.year) * 12 + mesActual.monthValue - primerMesBase.monthValue + 1
                    val primerMes = if (meses == 0 && mesesDeDatos > 60) mesActual.minusMonths(59) else primerMesBase
                    val formatoMes = DateTimeFormatter.ofPattern("MMM", Locale.forLanguageTag("es-PE"))

                    val atencionesRecientes = datos.atenciones.filter { atencion ->
                        val mes = YearMonth.from(atencion.fechaAtencion.atZone(zona))
                        mes in primerMes..mesActual
                    }
                    val cantidadPorMes = atencionesRecientes
                        .groupingBy { YearMonth.from(it.fechaAtencion.atZone(zona)) }
                        .eachCount()
                    val mesesGrafico = run {
                        val cantidadMeses = ((mesActual.year - primerMes.year) * 12 + mesActual.monthValue - primerMes.monthValue + 1).coerceIn(1, 60)
                        (0 until cantidadMeses).map { desplazamiento ->
                        val mes = primerMes.plusMonths(desplazamiento.toLong())
                        ConteoMensualPaciente(
                            periodo = mes,
                            etiqueta = mes.format(formatoMes).replaceFirstChar { it.uppercase() },
                            cantidad = cantidadPorMes[mes] ?: 0
                        )
                    }
                    }

                    val ahora = Instant.now()
                    val citasHistoricas = datos.citas.filter { cita ->
                        cita.inicioEpochMillis > 0 && cita.inicioEpochMillis < ahora.toEpochMilli() &&
                            YearMonth.from(Instant.ofEpochMilli(cita.inicioEpochMillis).atZone(zona)) in primerMes..mesActual
                    }
                    val atencionesPorCita = atencionesRecientes.map { it.citaId }.filter(String::isNotBlank).toSet()
                    val completadas = citasHistoricas.count { cita ->
                        cita.estado == EstadoCita.COMPLETADA || cita.id in atencionesPorCita
                    }
                    val inasistencias = citasHistoricas.count { cita ->
                        cita.id !in atencionesPorCita && cita.estado == EstadoCita.NO_ASISTIO
                    }
                    val canceladas = citasHistoricas.count { cita ->
                        cita.id !in atencionesPorCita && cita.estado == EstadoCita.CANCELADA
                    }
                    val sinResultado = citasHistoricas.count { cita ->
                        cita.id !in atencionesPorCita && cita.estado == EstadoCita.PROGRAMADA
                    }

                    _uiState.value = DashboardPacienteUiState(
                        cargando = false,
                        asistenciasPorMes = mesesGrafico,
                        estadosHistoricos = listOf(
                            EstadoCitaPaciente("Completadas", completadas),
                            EstadoCitaPaciente("No asistió", inasistencias),
                            EstadoCitaPaciente("Canceladas", canceladas),
                            EstadoCitaPaciente("Sin resultado", sinResultado)
                        ),
                        totalAtenciones = atencionesRecientes.size,
                        mesesFiltro = meses
                    )
                },
                onFailure = { error ->
                    _uiState.value = DashboardPacienteUiState(
                        cargando = false,
                        error = error.message ?: "No se pudo cargar tu resumen."
                    )
                }
            )
        }
    }
}
