package com.pe.terapia.domain.usecase.historiaclinica

import com.pe.terapia.domain.model.Atencion
import com.pe.terapia.domain.repository.HistoriaClinicaRepository

class ObtenerAtencionPorCitaUseCase (
    private val repository: HistoriaClinicaRepository
) {
    suspend operator fun invoke (citaId: String): Result<Atencion?>{
        return repository.obtenerAtencionPorCita(citaId)
    }
}