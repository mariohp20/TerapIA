package com.pe.terapia.domain.usecase.historiaclinica

import com.pe.terapia.domain.model.ProcesoTerapeutico
import com.pe.terapia.domain.repository.HistoriaClinicaRepository

class ObtenerProcesoActivoUseCase(
    private val repository: HistoriaClinicaRepository
) {
    suspend operator fun invoke(pacienteId: String): Result<ProcesoTerapeutico?> {
        return repository.obtenerProcesoActivo(pacienteId)
    }
}