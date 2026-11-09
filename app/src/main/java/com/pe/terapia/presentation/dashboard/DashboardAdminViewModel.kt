package com.pe.terapia.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pe.terapia.domain.model.CargaPsicologoAdmin
import com.pe.terapia.domain.model.DatoMensualAdmin
import com.pe.terapia.domain.usecase.dashboard.ObtenerDashboardAdminUseCase
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardAdminUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val meses: Int = 6,
    val totalCitas: Int = 0,
    val citasCompletadas: Int = 0,
    val ingresosPen: Double = 0.0,
    val ingresosPorMes: List<DatoMensualAdmin> = emptyList(),
    val citasPorMes: List<Pair<String, Pair<Int, Int>>> = emptyList(),
    val cargaPorPsicologo: List<CargaPsicologoAdmin> = emptyList()
)

class DashboardAdminViewModel(
    private val obtenerDashboardAdmin: ObtenerDashboardAdminUseCase
) : ViewModel() {
    private val zona = ZoneId.of("America/Lima")
    private val _uiState = MutableStateFlow(DashboardAdminUiState())
    val uiState: StateFlow<DashboardAdminUiState> = _uiState.asStateFlow()

    fun cargar(meses: Int = _uiState.value.meses) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null, meses = meses)
            val actual = YearMonth.now(zona)
            val primerMesConsulta = if (meses == 0) YearMonth.of(2000, 1) else actual.minusMonths((meses - 1).toLong())
            val desde = primerMesConsulta.atDay(1).atStartOfDay(zona).toInstant().toEpochMilli()
            val hasta = actual.plusMonths(1).atDay(1).atStartOfDay(zona).toInstant().toEpochMilli() - 1
            obtenerDashboardAdmin(desde, hasta).fold(
                onSuccess = { data ->
                    val format = DateTimeFormatter.ofPattern("MMM yy", Locale("es", "PE"))
                    val primerMesDatos = (data.citas.map { YearMonth.from(Instant.ofEpochMilli(it.inicioEpochMillis).atZone(zona)) } +
                        data.pagos.map { YearMonth.from(Instant.ofEpochMilli(it.fechaMillis).atZone(zona)) })
                        .filter { it <= actual }
                        .minOrNull() ?: actual
                    val primerMesGrafico = if (meses == 0) {
                        val mesesDeDatos = (actual.year - primerMesDatos.year) * 12 + actual.monthValue - primerMesDatos.monthValue + 1
                        if (mesesDeDatos > 60) actual.minusMonths(59) else primerMesDatos
                    } else actual.minusMonths((meses - 1).toLong())
                    val cantidadMeses = ((actual.year - primerMesGrafico.year) * 12 + actual.monthValue - primerMesGrafico.monthValue + 1).coerceIn(1, 60)
                    val months = (0 until cantidadMeses).map { primerMesGrafico.plusMonths(it.toLong()) }
                    val citasMonth = months.map { month ->
                        val rows = data.citas.filter { Instant.ofEpochMilli(it.inicioEpochMillis).atZone(zona).toLocalDate().let { d -> YearMonth.from(d) == month } }
                        month.format(format).replaceFirstChar(Char::uppercase) to
                            (rows.size to rows.count { it.estado == "COMPLETADA" })
                    }
                    val ingresosMonth = months.map { month ->
                        val amount = data.pagos.filter { it.moneda == "PEN" && Instant.ofEpochMilli(it.fechaMillis).atZone(zona).let { d -> YearMonth.from(d) == month } }.sumOf { it.monto }
                        DatoMensualAdmin(month.format(format).replaceFirstChar(Char::uppercase), amount)
                    }
                    val names = data.psicologos.associate { it.id to it.nombre }
                    val workload = data.citas.filter { it.estado == "COMPLETADA" }
                        .groupingBy { it.psicologoId }.eachCount()
                        .map { (id, count) -> CargaPsicologoAdmin(names[id] ?: "Psicólogo ${id.take(6)}", count) }
                        .sortedByDescending { it.atenciones }
                    _uiState.value = DashboardAdminUiState(
                        cargando = false,
                        meses = meses,
                        totalCitas = data.citas.size,
                        citasCompletadas = data.citas.count { it.estado == "COMPLETADA" },
                        ingresosPen = data.pagos.filter { it.moneda == "PEN" }.sumOf { it.monto },
                        ingresosPorMes = ingresosMonth,
                        citasPorMes = citasMonth,
                        cargaPorPsicologo = workload
                    )
                },
                onFailure = { error -> _uiState.value = _uiState.value.copy(cargando = false, error = error.message ?: "No se pudo cargar el dashboard.") }
            )
        }
    }
}
