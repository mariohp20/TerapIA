package com.pe.terapia.domain.usecase.citas

import com.pe.terapia.domain.model.CitaPacienteDetalle
import com.pe.terapia.domain.repository.CitaRepository

class ObtenerAgendaPsicologoPorFechaUseCase(private val repository: CitaRepository) {
    suspend operator fun invoke(psicologoId: String, fecha: String): Result<List<CitaPacienteDetalle>> =
        repository.obtenerAgendaPsicologoPorFecha(psicologoId, fecha)
}
