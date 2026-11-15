package com.pe.terapia.domain.usecase.dashboard

import com.pe.terapia.domain.model.*
import com.pe.terapia.domain.repository.DashboardRepository
import com.pe.terapia.domain.repository.HistoriaClinicaRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class ObtenerDashboardPsicologoUseCase(
    private val dashboardRepository: DashboardRepository,
    private val historiaRepository: HistoriaClinicaRepository
) {
    suspend operator fun invoke(psicologoId: String, mesesFiltro: Int = 6): Result<DashboardPsicologoData> = runCatching {
        require(psicologoId.isNotBlank()) { "No se pudo identificar al psicólogo." }
        coroutineScope {
            val atenciones = async { dashboardRepository.obtenerAtencionesPsicologo(psicologoId) }
            val etapas = async { dashboardRepository.obtenerEtapasActivasPsicologo(psicologoId) }
            val citas = async { dashboardRepository.obtenerCitasPorPsicologo(psicologoId) }
            val tiposEtapas = async { historiaRepository.obtenerTiposEtapas() }
            val atencionesResult = atenciones.await().getOrThrow()
            val etapasResult = etapas.await().getOrThrow()
            val citasResult = citas.await().getOrThrow()
            val tiposResult = tiposEtapas.await().getOrThrow()

            val zona = ZoneId.of("America/Lima")
            val actual = YearMonth.now(zona)
            val fechas = atencionesResult.map { YearMonth.from(it.fechaAtencion.atZone(zona)) } +
                citasResult.filter { it.inicioEpochMillis > 0 }.map { YearMonth.from(Instant.ofEpochMilli(it.inicioEpochMillis).atZone(zona)) }
            val primerMes = if (mesesFiltro > 0) {
                actual.minusMonths((mesesFiltro - 1).coerceAtLeast(0).toLong())
            } else {
                maxOf(fechas.minOrNull() ?: actual, actual.minusMonths(59))
            }
            val formato = DateTimeFormatter.ofPattern("MMM yy", Locale.forLanguageTag("es-PE"))
            val cantidadMeses = ((actual.year - primerMes.year) * 12 + actual.monthValue - primerMes.monthValue + 1).coerceIn(1, 60)
            val meses = (0 until cantidadMeses).map { primerMes.plusMonths(it.toLong()) }
            val atencionesEnPeriodo = atencionesResult.filter { YearMonth.from(it.fechaAtencion.atZone(zona)) in primerMes..actual }
            val citasEnPeriodo = citasResult.filter { it.inicioEpochMillis > 0 && YearMonth.from(Instant.ofEpochMilli(it.inicioEpochMillis).atZone(zona)) in primerMes..actual }
            val atencionesPorMes = atencionesEnPeriodo
                .groupingBy { YearMonth.from(it.fechaAtencion.atZone(zona)) }.eachCount()
            val citasPorMes = citasEnPeriodo
                .groupingBy { YearMonth.from(Instant.ofEpochMilli(it.inicioEpochMillis).atZone(zona)) }.eachCount()

            val nombresEtapas = tiposResult.associate { it.id to it.nombre }
            val etapasPorNombre = etapasResult.groupingBy { nombresEtapas[it.tipoEtapaId] ?: "Etapa sin nombre" }.eachCount()

            DashboardPsicologoData(
                atencionesPorMes = meses.map { mes -> ConteoMensualPsicologo(mes.toString(), mes.format(formato).replaceFirstChar(Char::uppercase), atencionesPorMes[mes] ?: 0) },
                pacientesPorEtapa = etapasPorNombre.map { (nombre, cantidad) -> ConteoEtapaPsicologo(nombre, cantidad) },
                comparativaAsistencia = meses.map { mes ->
                    ComparativaMensualPsicologo(mes.toString(), mes.format(formato).replaceFirstChar(Char::uppercase), citasPorMes[mes] ?: 0, atencionesPorMes[mes] ?: 0)
                }
            )
        }
    }
}
