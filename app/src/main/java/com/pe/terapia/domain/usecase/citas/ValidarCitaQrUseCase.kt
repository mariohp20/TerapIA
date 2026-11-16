package com.pe.terapia.domain.usecase.citas

import com.pe.terapia.domain.model.CitaQrInfo
import com.pe.terapia.domain.repository.CitaRepository

class ValidarCitaQrUseCase(private val citaRepository: CitaRepository) {
    suspend operator fun invoke(citaId: String, psicologoId: String): Result<CitaQrInfo> =
        citaRepository.validarCitaQr(citaId, psicologoId)
}
