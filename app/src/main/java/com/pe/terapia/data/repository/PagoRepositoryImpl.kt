package com.pe.terapia.data.repository

import com.pe.terapia.data.remote.firebase.PagoDataSource
import com.pe.terapia.domain.model.Comprobante
import com.pe.terapia.domain.model.Pago
import com.pe.terapia.domain.repository.PagoRepository
import kotlinx.coroutines.CancellationException

class PagoRepositoryImpl(private val dataSource: PagoDataSource) : PagoRepository {

    override suspend fun obtenerHistorialPagos(pacienteId: String): Result<List<Pair<Pago, Comprobante?>>> = capture {
        dataSource.obtenerHistorialPagos(pacienteId)
    }

    override suspend fun obtenerPagosPendientesPsicologo(psicologoId: String): Result<List<Pago>> = capture {
        dataSource.obtenerPagosPendientesPsicologo(psicologoId)
    }

    override suspend fun confirmarPagoClinica(pagoId: String): Result<Comprobante> = capture {
        dataSource.confirmarPagoClinica(pagoId)
    }

    override suspend fun procesarPagoOnline(pagoId: String, referenciaTransaccion: String): Result<Comprobante> = capture {
        dataSource.procesarPagoOnline(pagoId, referenciaTransaccion)
    }

    override suspend fun obtenerComprobantePorCita(citaId: String): Result<Comprobante?> = capture {
        dataSource.obtenerComprobantePorCita(citaId)
    }

    private suspend fun <T> capture(action: suspend () -> T): Result<T> = try {
        Result.success(action())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}
