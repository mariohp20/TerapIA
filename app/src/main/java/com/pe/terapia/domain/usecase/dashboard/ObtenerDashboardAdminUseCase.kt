package com.pe.terapia.domain.usecase.dashboard

import com.pe.terapia.domain.model.DashboardAdminData
import com.pe.terapia.domain.repository.DashboardRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ObtenerDashboardAdminUseCase(private val repository: DashboardRepository) {
    suspend operator fun invoke(desdeEpochMillis: Long, hastaEpochMillis: Long): Result<DashboardAdminData> = coroutineScope {
        val citasDef = async { repository.obtenerCitasAdmin(desdeEpochMillis, hastaEpochMillis) }
        val pagosDef = async { repository.obtenerPagosAdmin(desdeEpochMillis, hastaEpochMillis) }
        val psicologosDef = async { repository.obtenerPsicologosAdmin() }

        val citas = citasDef.await()
        val pagos = pagosDef.await()
        val psicologos = psicologosDef.await()

        if (citas.isFailure || pagos.isFailure || psicologos.isFailure) {
            Result.failure(Exception("Error al cargar los componentes del dashboard administrativo."))
        } else {
            Result.success(
                DashboardAdminData(
                    citas = citas.getOrThrow(),
                    pagos = pagos.getOrThrow(),
                    psicologos = psicologos.getOrThrow()
                )
            )
        }
    }
}