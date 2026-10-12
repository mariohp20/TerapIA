package com.pe.terapia.domain.usecase.pagos

import com.pe.terapia.domain.model.Comprobante
import com.pe.terapia.domain.repository.PagoRepository
import kotlinx.coroutines.CancellationException

class ConfirmarPagoClinicaUseCase(private val repository: PagoRepository) {
    suspend operator fun invoke(pagoId: String): Result<Comprobante> {
        if (pagoId.isBlank()) {
            return Result.failure(IllegalArgumentException("ID de pago inválido."))
        }
        return try {
            repository.confirmarPagoClinica(pagoId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
