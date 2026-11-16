package com.pe.terapia.domain.usecase.citas

import com.pe.terapia.domain.model.ProximaCita
import com.pe.terapia.domain.repository.CitaLocalRepository

class GuardarProximaCitaEnCacheUseCase(private val repository: CitaLocalRepository) {
    suspend operator fun invoke(pacienteId: String, cita: ProximaCita?): Result<Unit> =
        repository.sincronizarProximaCita(pacienteId, cita)
}
