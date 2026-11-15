package com.pe.terapia.domain.repository

import com.pe.terapia.domain.model.AtencionPsicologoResumen
import com.pe.terapia.domain.model.AtencionResumenDashboard
import com.pe.terapia.domain.model.Cita
import com.pe.terapia.domain.model.CitaAdminResumen
import com.pe.terapia.domain.model.DashboardAdminData
import com.pe.terapia.domain.model.EtapaPsicologoResumen
import com.pe.terapia.domain.model.PagoAdminResumen
import com.pe.terapia.domain.model.PsicologoAdminResumen

interface DashboardRepository {
    // Métodos para el Psicólogo
    suspend fun obtenerAtencionesPsicologo(psicologoId: String): Result<List<AtencionPsicologoResumen>>
    suspend fun obtenerEtapasActivasPsicologo(psicologoId: String): Result<List<EtapaPsicologoResumen>>
    suspend fun obtenerCitasPorPsicologo(psicologoId: String): Result<List<CitaAdminResumen>>

    // Métodos para el Paciente
    suspend fun obtenerAtencionesPaciente(pacienteId: String): Result<List<AtencionResumenDashboard>>
    suspend fun obtenerCitasPorPaciente(pacienteId: String): Result<List<Cita>>

    // Métodos para el Administrador
    suspend fun obtenerCitasAdmin(desdeEpochMillis: Long, hastaEpochMillis: Long): Result<List<CitaAdminResumen>>
    suspend fun obtenerPagosAdmin(desdeEpochMillis: Long, hastaEpochMillis: Long): Result<List<PagoAdminResumen>>
    suspend fun obtenerPsicologosAdmin(): Result<List<PsicologoAdminResumen>>

}
