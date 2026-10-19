package com.pe.terapia.core.pdf.Plantillas

data class ComprobanteData(
    val numero: String,
    val paciente: String,
    val fechaPago: String,
    val concepto: String,
    val monto: String,
    val metodoPago: String,
    val codigoOperacion: String
)