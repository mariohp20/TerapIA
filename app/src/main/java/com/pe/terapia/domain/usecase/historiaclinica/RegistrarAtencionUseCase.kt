package com.pe.terapia.domain.usecase.historiaclinica

import com.pe.terapia.domain.model.Atencion
import com.pe.terapia.domain.repository.HistoriaClinicaRepository

class RegistrarAtencionUseCase(
    private val repository: HistoriaClinicaRepository
) {
    suspend operator fun invoke(atencion: Atencion): Result<Unit> {
        if (atencion.citaId.isBlank() || atencion.pacienteId.isBlank() || atencion.psicologoId.isBlank()) {
            return Result.failure(Exception("Faltan datos de identificación para registrar la atención."))
        }
        if (atencion.motivoConsulta.isBlank() && atencion.evolucion.isBlank()) {
            return Result.failure(Exception("Debe ingresar al menos el motivo de consulta o la evolución."))
        }

        return repository.registrarAtencion(atencion)
    }
}