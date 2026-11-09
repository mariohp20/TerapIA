package com.pe.terapia.domain.usecase.dashboard

import com.pe.terapia.domain.model.Atencion
import com.pe.terapia.domain.repository.DashboardRepository

class ObtenerAtencionesPsicologoUseCase(
    private val repository: DashboardRepository
) {
    suspend operator fun invoke(psicologoId: String): Result<List<Atencion>> {
        return repository.obtenerAtencionesPsicologo(psicologoId)
    }
}