package com.pe.terapia.domain.usecase.historiaclinica

import com.pe.terapia.domain.model.PacienteResumen
import com.pe.terapia.domain.repository.HistoriaClinicaRepository

class ObtenerPacientesDePsicologoUseCase(
    private val repository: HistoriaClinicaRepository
) {
    suspend operator fun invoke(psicologoId: String): Result<List<PacienteResumen>> {
        return repository.obtenerPacientesDelPsicologo(psicologoId)
    }
}