package com.pe.terapia.domain.usecase.citas

import com.pe.terapia.domain.repository.CitaRepository

class ObtenerCitasPendientesResultadoUseCase(private val repository: CitaRepository) {
    suspend operator fun invoke(psicologoId: String, ahoraEpochMillis: Long = System.currentTimeMillis()) =
        repository.obtenerCitasPendientesResultado(psicologoId, ahoraEpochMillis)
}
