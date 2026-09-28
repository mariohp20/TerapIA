package com.pe.terapia.domain.usecase.cita

import com.pe.terapia.domain.model.Cita
import com.pe.terapia.domain.repository.CitaRepository
import com.pe.terapia.domain.repository.HistoriaClinicaRepository

class ReservarCitaUseCase (
    private val citaRepository: CitaRepository,
    private val historiaRepository: HistoriaClinicaRepository
) {
    suspend operator fun invoke(cita: Cita): Result<Boolean> {
        return try {
            // 1. Mandamos a guardar la cita
            val reservaExitosa = citaRepository.reservarCita(cita).getOrThrow()

            // 2. Si la cita se guardó bien, disparamos el "trigger" de la historia clínica
            if (reservaExitosa) {
                historiaRepository.verificarOCrearHistoria(cita.pacienteId).getOrThrow()
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}