package com.pe.terapia.data.repository

import com.pe.terapia.data.remote.firebase.CitaDataSource
import com.pe.terapia.domain.model.Cita
import com.pe.terapia.domain.model.Especialidad
import com.pe.terapia.domain.model.Reserva
import com.pe.terapia.domain.model.Sede
import com.pe.terapia.domain.model.SlotDisponible
import com.pe.terapia.domain.model.Psicologo
import com.pe.terapia.domain.model.ProximaCita
import com.pe.terapia.domain.repository.CitaRepository
import kotlinx.coroutines.CancellationException

class CitaRepositoryImpl(private val dataSource: CitaDataSource) : CitaRepository {
    override suspend fun obtenerEspecialidades(): Result<List<Especialidad>> = capture {
        dataSource.obtenerEspecialidades()
    }

    override suspend fun obtenerPsicologosPorEspecialidad(especialidadId: String): Result<List<Psicologo>> = capture {
        dataSource.obtenerPsicologosPorEspecialidad(especialidadId)
    }

    override suspend fun obtenerSedes(psicologoId: String): Result<List<Sede>> = capture {
        dataSource.obtenerSedes(psicologoId)
    }

    override suspend fun obtenerDisponibilidad(
        psicologoId: String,
        sedeId: String,
        fecha: String
    ): Result<List<SlotDisponible>> = capture {
        dataSource.obtenerDisponibilidad(psicologoId, sedeId, fecha)
    }

    override suspend fun obtenerProximaCita(pacienteId: String): Result<ProximaCita?> = capture {
        dataSource.obtenerProximaCita(pacienteId)
    }

    override suspend fun reservarYConfirmar(reserva: Reserva): Result<Cita> = capture {
        dataSource.reservarYConfirmar(reserva)
    }

    private suspend fun <T> capture(action: suspend () -> T): Result<T> = try {
        Result.success(action())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}
