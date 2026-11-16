package com.pe.terapia.domain.usecase.pagos

import com.pe.terapia.domain.model.Pago
import com.pe.terapia.domain.repository.PagoRepository

class ObtenerPagoPorCitaUseCase(private val repository: PagoRepository) {
    suspend operator fun invoke(citaId: String): Result<Pago?> {
        if (citaId.isBlank()) {
            return Result.failure(IllegalArgumentException("No se pudo identificar la cita."))
        }
        return repository.obtenerPagoPorCita(citaId)
    }
}
