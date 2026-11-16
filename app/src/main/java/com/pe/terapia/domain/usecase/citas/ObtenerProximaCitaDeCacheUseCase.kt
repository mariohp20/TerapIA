package com.pe.terapia.domain.usecase.citas

import com.pe.terapia.domain.model.ProximaCita
import com.pe.terapia.domain.repository.CitaLocalRepository

class ObtenerProximaCitaDeCacheUseCase(private val repository: CitaLocalRepository) {
    suspend operator fun invoke(pacienteId: String, ahoraMillis: Long): Result<ProximaCita?> =
        repository.obtenerProximaCita(pacienteId, ahoraMillis)
}
