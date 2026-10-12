package com.pe.terapia.domain.usecase.pagos

import com.pe.terapia.domain.model.Comprobante
import com.pe.terapia.domain.repository.PagoRepository
import kotlinx.coroutines.CancellationException

class PagarOnlineUseCase(private val repository: PagoRepository) {
    suspend operator fun invoke(pagoId: String, referenciaTransaccion: String): Result<Comprobante> {
        if (pagoId.isBlank() || referenciaTransaccion.isBlank()) {
            return Result.failure(IllegalArgumentException("Datos de pago online incompletos."))
        }
        return try {
            repository.procesarPagoOnline(pagoId, referenciaTransaccion)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
