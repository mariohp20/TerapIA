package com.pe.terapia.domain.usecase.historiaclinica

import com.pe.terapia.domain.model.Atencion
import com.pe.terapia.domain.repository.HistoriaClinicaRepository

class ObtenerAtencionesPacienteUseCase(
    private val repository: HistoriaClinicaRepository
) {
    suspend operator fun invoke(pacienteId: String): Result<List<Atencion>> {
        return repository.obtenerAtencionesPorPaciente(pacienteId)
    }
}