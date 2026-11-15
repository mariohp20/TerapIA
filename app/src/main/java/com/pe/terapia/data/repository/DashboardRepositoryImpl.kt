package com.pe.terapia.data.repository

import com.pe.terapia.data.remote.firebase.DashboardDataSource
import com.pe.terapia.domain.model.AtencionPsicologoResumen
import com.pe.terapia.domain.repository.DashboardRepository
import com.pe.terapia.domain.model.Cita
import com.pe.terapia.domain.model.AtencionResumenDashboard
import com.pe.terapia.domain.model.CitaAdminResumen
import com.pe.terapia.domain.model.DashboardAdminData
import com.pe.terapia.domain.model.EtapaPsicologoResumen
import com.pe.terapia.domain.model.PagoAdminResumen
import com.pe.terapia.domain.model.PsicologoAdminResumen

class DashboardRepositoryImpl(
    private val dataSource: DashboardDataSource
) : DashboardRepository {

    override suspend fun obtenerAtencionesPsicologo(psicologoId: String): Result<List<AtencionPsicologoResumen>> = runCatching {
        dataSource.obtenerAtencionesPorPsicologo(psicologoId)
    }

    override suspend fun obtenerEtapasActivasPsicologo(psicologoId: String): Result<List<EtapaPsicologoResumen>> = runCatching {
        dataSource.obtenerEtapasActivasPorPsicologo(psicologoId)
    }
    override suspend fun obtenerCitasPorPsicologo(psicologoId: String): Result<List<CitaAdminResumen>> = runCatching {
        dataSource.obtenerCitasPorPsicologo(psicologoId)
    }

    override suspend fun obtenerAtencionesPaciente(pacienteId: String): Result<List<AtencionResumenDashboard>> = runCatching {
        dataSource.obtenerAtencionesPorPaciente(pacienteId)
    }

    override suspend fun obtenerCitasPorPaciente(pacienteId: String): Result<List<Cita>> = runCatching {
        dataSource.obtenerCitasPorPaciente(pacienteId)
    }

    override suspend fun obtenerCitasAdmin(desdeEpochMillis: Long, hastaEpochMillis: Long): Result<List<CitaAdminResumen>> = runCatching {
        dataSource.obtenerCitasAdmin(desdeEpochMillis, hastaEpochMillis)
    }
    override suspend fun obtenerPagosAdmin(desdeEpochMillis: Long, hastaEpochMillis: Long): Result<List<PagoAdminResumen>> = runCatching {
        dataSource.obtenerPagosAdmin(desdeEpochMillis, hastaEpochMillis)
    }
    override suspend fun obtenerPsicologosAdmin(): Result<List<PsicologoAdminResumen>> = runCatching {
        dataSource.obtenerPsicologosAdmin()
    }
}
