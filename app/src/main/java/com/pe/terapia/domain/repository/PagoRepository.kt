package com.pe.terapia.domain.repository

import com.pe.terapia.domain.model.Comprobante
import com.pe.terapia.domain.model.Pago

interface PagoRepository {
    suspend fun obtenerHistorialPagos(pacienteId: String): Result<List<Pair<Pago, Comprobante?>>>
    suspend fun obtenerPagosPendientesPsicologo(psicologoId: String): Result<List<Pago>>
    suspend fun confirmarPagoClinica(pagoId: String): Result<Comprobante>
    suspend fun procesarPagoOnline(pagoId: String, referenciaTransaccion: String): Result<Comprobante>
    suspend fun obtenerComprobantePorCita(citaId: String): Result<Comprobante?>
}
