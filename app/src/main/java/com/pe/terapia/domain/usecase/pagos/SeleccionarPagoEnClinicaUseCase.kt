package com.pe.terapia.domain.usecase.pagos

import com.pe.terapia.domain.repository.PagoRepository

class SeleccionarPagoEnClinicaUseCase(private val repository: PagoRepository) {
    suspend operator fun invoke(pagoId: String): Result<Unit> {
        if (pagoId.isBlank()) {
            return Result.failure(IllegalArgumentException("No se pudo identificar el pago."))
        }
        return repository.seleccionarPagoEnClinica(pagoId)
    }
}
