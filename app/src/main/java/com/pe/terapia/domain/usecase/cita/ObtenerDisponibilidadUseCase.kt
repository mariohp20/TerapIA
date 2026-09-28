package com.pe.terapia.domain.usecase.cita

import com.pe.terapia.domain.repository.CitaRepository

class ObtenerDisponibilidadUseCase (private val repository: CitaRepository) {
    suspend operator fun invoke(psicologoId: String, fecha: String): Result<List<String>> {
        if (psicologoId.isBlank() || fecha.isBlank()) {
            return Result.failure(IllegalArgumentException("El ID del psicólogo y la fecha son obligatorios."))
        }
        return repository.obtenerDisponibilidad(psicologoId, fecha)
    }
}