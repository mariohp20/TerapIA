package com.pe.terapia.domain.usecase.citas

import com.pe.terapia.domain.model.ProximaCita
import com.pe.terapia.domain.repository.CitaRepository

class ObtenerProximaCitaUseCase(private val repository: CitaRepository) {
    suspend operator fun invoke(pacienteId: String): Result<ProximaCita?> {
        if (pacienteId.isBlank()) return Result.failure(IllegalArgumentException("No se pudo identificar al paciente."))
        return repository.obtenerProximaCita(pacienteId)
    }
}
