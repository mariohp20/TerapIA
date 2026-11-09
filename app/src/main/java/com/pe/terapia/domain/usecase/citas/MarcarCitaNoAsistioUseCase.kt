package com.pe.terapia.domain.usecase.citas

import com.pe.terapia.domain.repository.CitaRepository

class MarcarCitaNoAsistioUseCase(private val repository: CitaRepository) {
    suspend operator fun invoke(citaId: String, psicologoId: String, ahoraEpochMillis: Long = System.currentTimeMillis()) =
        repository.marcarCitaNoAsistio(citaId, psicologoId, ahoraEpochMillis)
}
