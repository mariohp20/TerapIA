package com.pe.terapia.domain.usecase.dashboard

import com.pe.terapia.domain.model.Cita
import com.pe.terapia.domain.repository.DashboardRepository

class ObtenerCitasPsicologoUseCase(
    private val repository: DashboardRepository
) {
    suspend operator fun invoke(psicologoId: String): Result<List<Cita>> {
        return repository.obtenerCitasPorPsicologo(psicologoId)
    }
}