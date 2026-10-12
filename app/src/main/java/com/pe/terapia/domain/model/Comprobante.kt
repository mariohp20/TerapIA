package com.pe.terapia.domain.model

data class Comprobante(
    val id: String = "",
    val pagoId: String = "",
    val citaId: String = "",
    val pacienteId: String = "",
    val monto: Double = PRECIO_ESTANDAR_CITA,
    val moneda: String = "PEN",
    val fechaEmision: String = "",
    val tipo: String = "BOLETA",
    val numeroComprobante: String = "",
    val detalles: String = ""
)
