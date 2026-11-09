package com.pe.terapia.domain.usecase.dashboard

import com.pe.terapia.domain.model.EtapaClinica
import com.pe.terapia.domain.repository.DashboardRepository

class ObtenerEtapasActivasPsicologoUseCase(
    private val repository: DashboardRepository
) {
    suspend operator fun invoke(psicologoId: String): Result<List<EtapaClinica>> {
        return repository.obtenerEtapasActivasPsicologo(psicologoId)
    }
}