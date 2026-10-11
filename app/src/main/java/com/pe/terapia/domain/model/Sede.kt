package com.pe.terapia.domain.model

data class Sede(
    val id: String,
    val nombre: String,
    val direccion: String,
    val distrito: String,
    val ciudad: String,
    val latitud: Double,
    val longitud: Double,
    val telefono: String,
    val activa: Boolean
)
