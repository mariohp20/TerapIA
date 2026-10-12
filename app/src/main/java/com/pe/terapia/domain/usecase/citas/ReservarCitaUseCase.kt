package com.pe.terapia.domain.usecase.citas

import com.pe.terapia.domain.model.Cita
import com.pe.terapia.domain.model.Reserva
import com.pe.terapia.domain.repository.CitaRepository
import kotlinx.coroutines.CancellationException

class ReservarCitaUseCase(
    private val repository: CitaRepository,
) {
    suspend operator fun invoke(reserva: Reserva): Result<Cita> {
        if (
            reserva.pacienteId.isBlank() || reserva.psicologoId.isBlank() || reserva.sedeId.isBlank() ||
            reserva.horarioId.isBlank() || reserva.bloqueId.isBlank() || reserva.especialidadId.isBlank() ||
            reserva.fecha.isBlank() || reserva.hora.isBlank() || reserva.inicioEpochMillis <= 0
        ) {
            return Result.failure(IllegalArgumentException("Faltan datos para confirmar la cita."))
        }

        return try {
            val resultadoCita = repository.reservarYConfirmar(reserva)
            resultadoCita
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
