package com.pe.terapia.domain.repository

import com.pe.terapia.domain.model.Atencion

interface HistoriaClinicaRepository {
    suspend fun registrarAtencion(atencion: Atencion): Result<Unit>

    suspend fun obtenerAtencionesPorPaciente(pacienteId: String): Result<List<Atencion>>

    suspend fun obtenerPacientesDelPsicologo(psicologoId: String): Result<List<com.pe.terapia.domain.model.PacienteResumen>>

}
