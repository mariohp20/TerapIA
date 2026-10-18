package com.pe.terapia.domain.repository

import com.pe.terapia.domain.model.Atencion
import com.pe.terapia.domain.model.TipoEtapa

interface HistoriaClinicaRepository {
    suspend fun obtenerAtencionPorCita(citaId: String):Result<Atencion?>

    suspend fun registrarAtencion(atencion: Atencion): Result<Unit>

    suspend fun obtenerTiposEtapas(): Result<List<TipoEtapa>>

    suspend fun obtenerAtencionesPorPaciente(pacienteId: String): Result<List<Atencion>>

    suspend fun obtenerPacientesDelPsicologo(psicologoId: String): Result<List<com.pe.terapia.domain.model.PacienteResumen>>

}
