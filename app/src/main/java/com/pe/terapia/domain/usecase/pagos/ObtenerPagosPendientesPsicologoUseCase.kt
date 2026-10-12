package com.pe.terapia.domain.usecase.pagos

import com.pe.terapia.domain.model.Pago
import com.pe.terapia.domain.repository.PagoRepository
import kotlinx.coroutines.CancellationException

class ObtenerPagosPendientesPsicologoUseCase(private val repository: PagoRepository) {
    suspend operator fun invoke(psicologoId: String): Result<List<Pago>> {
        if (psicologoId.isBlank()) {
            return Result.failure(IllegalArgumentException("ID de psicólogo inválido."))
        }
        return try {
            repository.obtenerPagosPendientesPsicologo(psicologoId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
