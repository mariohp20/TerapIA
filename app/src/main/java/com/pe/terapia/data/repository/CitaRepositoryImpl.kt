package com.pe.terapia.data.repository

import com.pe.terapia.data.remote.firebase.CitaDataSource
import com.pe.terapia.domain.model.Cita
import com.pe.terapia.domain.repository.CitaRepository

class CitaRepositoryImpl (private val dataSource: CitaDataSource) : CitaRepository {
    override suspend fun obtenerDisponibilidad(psicologoId: String, fecha: String): Result<List<String>> {
        return try {
            val horasOcupadas = dataSource.obtenerDisponibilidad(psicologoId, fecha)
            Result.success(horasOcupadas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reservarCita(cita: Cita): Result<Boolean> {
        return try {
            val exito = dataSource.reservarCita(cita)
            Result.success(exito)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}