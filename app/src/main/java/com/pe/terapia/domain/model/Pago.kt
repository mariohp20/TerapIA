package com.pe.terapia.domain.model

const val PRECIO_ESTANDAR_CITA: Double = 50.0

data class Pago(
    val id: String = "",
    val citaId: String = "",
    val pacienteId: String = "",
    val psicologoId: String = "",
    val monto: Double = PRECIO_ESTANDAR_CITA,
    val moneda: String = "PEN",
    val estado: EstadoPago = EstadoPago.PENDIENTE,
    val metodoPago: MetodoPago? = null,
    val referenciaTransaccion: String? = null,
    val creadaEn: Long = System.currentTimeMillis(),
    val actualizadaEn: Long? = null
)
