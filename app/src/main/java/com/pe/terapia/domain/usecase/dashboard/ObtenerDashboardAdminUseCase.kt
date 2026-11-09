package com.pe.terapia.domain.usecase.dashboard

import com.pe.terapia.domain.model.DashboardAdminData
import com.pe.terapia.domain.repository.DashboardRepository

class ObtenerDashboardAdminUseCase(private val repository: DashboardRepository) {
    suspend operator fun invoke(desdeEpochMillis: Long, hastaEpochMillis: Long): Result<DashboardAdminData> =
        repository.obtenerDatosAdmin(desdeEpochMillis, hastaEpochMillis)
}
