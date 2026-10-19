package com.pe.terapia.domain.model

data class ProximaCita(
    val psicologoNombre: String,
    val especialidadNombre: String,
    val fecha: String,
    val hora: String,
    val sedeNombre: String,
    val sedeDireccion: String,
    val estadoPago: String = "PENDIENTE",
    val id: String = "",
    val estado: String = "",
    val inicioEpochMillis: Long = 0L,
    val psicologoDescripcion: String = "",
    val sedeLatitud: Double = 0.0,
    val sedeLongitud: Double = 0.0
)