package com.pe.terapia.data.repository

import com.pe.terapia.data.local.room.CitaDao
import com.pe.terapia.data.local.room.CitaEntity
import com.pe.terapia.domain.model.ProximaCita
import com.pe.terapia.domain.repository.CitaLocalRepository
import kotlinx.coroutines.CancellationException

class CitaLocalRepositoryImpl(private val citaDao: CitaDao) : CitaLocalRepository {
    override suspend fun sincronizarProximaCita(pacienteId: String, cita: ProximaCita?): Result<Unit> = capture {
        require(pacienteId.isNotBlank()) { "No se pudo identificar al paciente." }
        val citaEntity = cita?.let {
            require(it.id.isNotBlank() && it.inicioEpochMillis > 0L) { "La cita no tiene identificador o fecha válida." }
            CitaEntity(
                id = it.id,
                pacienteId = pacienteId,
                psicologoNombre = it.psicologoNombre,
                especialidadNombre = it.especialidadNombre,
                sedeNombre = it.sedeNombre,
                sedeDireccion = it.sedeDireccion,
                fecha = it.fecha,
                hora = it.hora,
                inicioEpochMillis = it.inicioEpochMillis,
                estado = it.estado,
                estadoPago = it.estadoPago
            )
        }
        citaDao.reemplazarProximaCita(pacienteId, citaEntity)
    }

    override suspend fun obtenerProximaCita(pacienteId: String, ahoraMillis: Long): Result<ProximaCita?> = capture {
        require(pacienteId.isNotBlank()) { "No se pudo identificar al paciente." }
        citaDao.obtenerProximaCitaLocal(pacienteId, ahoraMillis)?.let { local ->
            ProximaCita(
                id = local.id,
                psicologoNombre = local.psicologoNombre,
                especialidadNombre = local.especialidadNombre,
                fecha = local.fecha,
                hora = local.hora,
                sedeNombre = local.sedeNombre,
                sedeDireccion = local.sedeDireccion,
                inicioEpochMillis = local.inicioEpochMillis,
                estado = local.estado,
                estadoPago = local.estadoPago
            )
        }
    }

    private suspend fun <T> capture(action: suspend () -> T): Result<T> = try {
        Result.success(action())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}
