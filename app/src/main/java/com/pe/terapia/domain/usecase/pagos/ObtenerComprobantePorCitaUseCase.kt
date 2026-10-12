package com.pe.terapia.domain.usecase.pagos

import com.pe.terapia.domain.model.Comprobante
import com.pe.terapia.domain.repository.PagoRepository
import kotlinx.coroutines.CancellationException

class ObtenerComprobantePorCitaUseCase(private val repository: PagoRepository) {
    suspend operator fun invoke(citaId: String): Result<Comprobante?> {
        if (citaId.isBlank()) {
            return Result.failure(IllegalArgumentException("ID de cita inválido."))
        }
        return try {
            repository.obtenerComprobantePorCita(citaId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
