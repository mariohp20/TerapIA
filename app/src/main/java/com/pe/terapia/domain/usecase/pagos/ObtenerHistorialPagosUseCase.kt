package com.pe.terapia.domain.usecase.pagos

import com.pe.terapia.domain.model.Comprobante
import com.pe.terapia.domain.model.Pago
import com.pe.terapia.domain.repository.PagoRepository
import kotlinx.coroutines.CancellationException

class ObtenerHistorialPagosUseCase(private val repository: PagoRepository) {
    suspend operator fun invoke(pacienteId: String): Result<List<Pair<Pago, Comprobante?>>> {
        if (pacienteId.isBlank()) {
            return Result.failure(IllegalArgumentException("ID de paciente inválido."))
        }
        return try {
            repository.obtenerHistorialPagos(pacienteId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
