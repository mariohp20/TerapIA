package com.pe.terapia.data.repository

import com.pe.terapia.data.remote.firebase.DashboardDataSource
import com.pe.terapia.domain.model.Atencion
import com.pe.terapia.domain.model.EtapaClinica
import com.pe.terapia.domain.repository.DashboardRepository
import com.pe.terapia.domain.model.Cita

class DashboardRepositoryImpl(
    private val dataSource: DashboardDataSource
) : DashboardRepository {

    override suspend fun obtenerAtencionesPsicologo(psicologoId: String): Result<List<Atencion>> = runCatching {
        dataSource.obtenerAtencionesPorPsicologo(psicologoId)
    }

    override suspend fun obtenerEtapasActivasPsicologo(psicologoId: String): Result<List<EtapaClinica>> = runCatching {
        dataSource.obtenerEtapasActivasPorPsicologo(psicologoId)
    }
    override suspend fun obtenerCitasPorPsicologo(psicologoId: String): Result<List<Cita>> = runCatching {
        dataSource.obtenerCitasPorPsicologo(psicologoId)
    }
}