package com.pe.terapia.domain.usecase.citas

import com.pe.terapia.domain.model.SlotDisponible
import com.pe.terapia.domain.model.Especialidad
import com.pe.terapia.domain.model.Sede
import com.pe.terapia.domain.model.Psicologo
import com.pe.terapia.domain.repository.CitaRepository

class ObtenerDisponibilidadUseCase(private val repository: CitaRepository) {
    suspend fun especialidades(): Result<List<Especialidad>> = repository.obtenerEspecialidades()

    suspend fun psicologosPorEspecialidad(especialidadId: String): Result<List<Psicologo>> {
        if (especialidadId.isBlank()) {
            return Result.failure(IllegalArgumentException("Selecciona una especialidad."))
        }
        return repository.obtenerPsicologosPorEspecialidad(especialidadId)
    }

    suspend fun sedes(psicologoId: String): Result<List<Sede>> {
        if (psicologoId.isBlank()) return Result.failure(IllegalArgumentException("Selecciona un profesional."))
        return repository.obtenerSedes(psicologoId)
    }

    suspend fun horariosDisponibles(psicologoId: String, sedeId: String, fecha: String): Result<List<SlotDisponible>> {
        if (psicologoId.isBlank() || sedeId.isBlank() || fecha.isBlank()) {
            return Result.failure(IllegalArgumentException("Selecciona un profesional, una sede y una fecha."))
        }
        return repository.obtenerDisponibilidad(psicologoId, sedeId, fecha)
    }
}
