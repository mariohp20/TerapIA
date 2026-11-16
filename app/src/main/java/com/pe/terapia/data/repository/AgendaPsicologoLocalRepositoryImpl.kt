package com.pe.terapia.data.repository

import com.pe.terapia.data.local.room.AgendaPsicologoDao
import com.pe.terapia.data.local.room.AgendaPsicologoEntity
import com.pe.terapia.domain.model.CitaPacienteDetalle
import com.pe.terapia.domain.repository.AgendaPsicologoLocalRepository

class AgendaPsicologoLocalRepositoryImpl(private val dao: AgendaPsicologoDao) : AgendaPsicologoLocalRepository {
    override suspend fun obtener(psicologoId: String, fecha: String): List<CitaPacienteDetalle> =
        dao.obtener(psicologoId, fecha).map {
            CitaPacienteDetalle(it.citaId, it.pacienteId, it.hora, it.nombrePaciente, it.fecha, it.inicioEpochMillis)
        }

    override suspend fun guardar(psicologoId: String, fecha: String, citas: List<CitaPacienteDetalle>) {
        dao.reemplazar(psicologoId, fecha, citas.map {
            AgendaPsicologoEntity(psicologoId, fecha, it.citaId, it.pacienteId, it.hora, it.nombrePaciente, it.inicioEpochMillis)
        })
    }
}
