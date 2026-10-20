package com.pe.terapia.domain.usecase.historiaclinica

import com.pe.terapia.domain.repository.HistoriaClinicaRepository

class TransicionarEtapaUseCase(
    private val repository: HistoriaClinicaRepository
) {
    suspend operator fun invoke(
        procesoId: String,
        etapaActualId: String,
        nuevoTipoEtapaId: String,
        observaciones: String
    ): Result<Unit> {
        return repository.transicionarEtapaClinica(procesoId, etapaActualId, nuevoTipoEtapaId, observaciones)
    }
}