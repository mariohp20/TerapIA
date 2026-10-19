package com.pe.terapia.domain.usecase.historiaclinica

import com.pe.terapia.domain.model.EtapaClinica
import com.pe.terapia.domain.repository.HistoriaClinicaRepository

class ObtenerEtapasPorProcesoUseCase(
    private val repository: HistoriaClinicaRepository
) {
    suspend operator fun invoke(procesoId: String): Result<List<EtapaClinica>> {
        return repository.obtenerEtapasPorProceso(procesoId)
    }
}