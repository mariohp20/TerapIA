package com.pe.terapia.data.repository

import com.pe.terapia.data.remote.firebase.HistoriaClinicaDataSource
import com.pe.terapia.domain.model.Atencion
import com.pe.terapia.domain.repository.HistoriaClinicaRepository

class HistoriaClinicaRepositoryImpl(
    private val dataSource: HistoriaClinicaDataSource
) : HistoriaClinicaRepository {

    override suspend fun registrarAtencion(atencion: Atencion): Result<Unit> = runCatching {
        dataSource.registrarAtencionYCrearHistoriaSiNoExiste(atencion)
    }
}
