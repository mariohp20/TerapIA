package com.pe.terapia.domain.usecase.historiaclinica

import com.pe.terapia.domain.model.TipoEtapa
import com.pe.terapia.domain.repository.HistoriaClinicaRepository

class ObtenerTiposEtapasUseCase(
    private val repository: HistoriaClinicaRepository
) {
    suspend operator fun invoke(): Result<List<TipoEtapa>> {
        return repository.obtenerTiposEtapas()
    }
}