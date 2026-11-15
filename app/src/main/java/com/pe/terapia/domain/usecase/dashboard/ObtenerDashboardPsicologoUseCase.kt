package com.pe.terapia.domain.usecase.dashboard

import com.pe.terapia.domain.model.AtencionPsicologoResumen
import com.pe.terapia.domain.model.CitaAdminResumen
import com.pe.terapia.domain.model.EtapaPsicologoResumen
import com.pe.terapia.domain.model.TipoEtapa
import com.pe.terapia.domain.repository.DashboardRepository
import com.pe.terapia.domain.usecase.historiaclinica.ObtenerTiposEtapasUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class DatosDashboardPsicologo(
    val atenciones: List<AtencionPsicologoResumen>,
    val etapasActivas: List<EtapaPsicologoResumen>,
    val citas: List<CitaAdminResumen>,
    val tiposEtapas: List<TipoEtapa>
)

class ObtenerDashboardPsicologoUseCase(
    private val repository: DashboardRepository,
    private val obtenerTiposEtapasUseCase: ObtenerTiposEtapasUseCase
) {
    suspend operator fun invoke(psicologoId: String): Result<DatosDashboardPsicologo> = coroutineScope {


        val atencionesDef = async { repository.obtenerAtencionesPsicologo(psicologoId) }
        val etapasDef = async { repository.obtenerEtapasActivasPsicologo(psicologoId) }
        val citasDef = async { repository.obtenerCitasPorPsicologo(psicologoId) }
        val tiposDef = async { obtenerTiposEtapasUseCase() }

        val atenciones = atencionesDef.await()
        val etapas = etapasDef.await()
        val citas = citasDef.await()
        val tipos = tiposDef.await()

        if (atenciones.isFailure || etapas.isFailure || citas.isFailure || tipos.isFailure) {
            Result.failure(Exception("Error al cargar uno o más componentes del dashboard del psicólogo."))
        } else {
            Result.success(
                DatosDashboardPsicologo(
                    atenciones = atenciones.getOrThrow(),
                    etapasActivas = etapas.getOrThrow(),
                    citas = citas.getOrThrow(),
                    tiposEtapas = tipos.getOrThrow()
                )
            )
        }
    }
}