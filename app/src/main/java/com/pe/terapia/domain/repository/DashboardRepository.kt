package com.pe.terapia.domain.repository

import com.pe.terapia.domain.model.Atencion
import com.pe.terapia.domain.model.AtencionResumenDashboard
import com.pe.terapia.domain.model.Cita
import com.pe.terapia.domain.model.EtapaClinica
import com.pe.terapia.domain.model.DashboardAdminData

interface DashboardRepository {
    // Métodos para el Psicólogo
    suspend fun obtenerAtencionesPsicologo(psicologoId: String): Result<List<Atencion>>
    suspend fun obtenerEtapasActivasPsicologo(psicologoId: String): Result<List<EtapaClinica>>
    suspend fun obtenerCitasPorPsicologo(psicologoId: String): Result<List<Cita>>

    // Métodos para el Paciente
    suspend fun obtenerAtencionesPaciente(pacienteId: String): Result<List<AtencionResumenDashboard>>
    suspend fun obtenerCitasPorPaciente(pacienteId: String): Result<List<Cita>>

    suspend fun obtenerDatosAdmin(desdeEpochMillis: Long, hastaEpochMillis: Long): Result<DashboardAdminData>

    // Métodos para el Administrador (Sin filtros de ID, trae todo)

}
