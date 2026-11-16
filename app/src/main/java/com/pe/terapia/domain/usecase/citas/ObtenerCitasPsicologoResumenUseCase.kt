package com.pe.terapia.domain.usecase.citas

import com.pe.terapia.domain.model.CitasPsicologoResumen
import com.pe.terapia.domain.repository.CitaRepository

class ObtenerCitasPsicologoResumenUseCase(private val repository: CitaRepository) {
    suspend operator fun invoke(psicologoId: String, ahoraEpochMillis: Long): Result<CitasPsicologoResumen> =
        repository.obtenerCitasPsicologoResumen(psicologoId, ahoraEpochMillis)
}
