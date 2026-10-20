package com.pe.terapia.domain.model

data class Especialidad(
    val id: String,
    val nombre: String,
    val descripcion: String = "",
    val activa: Boolean = true,
    val costo_cita: Double
)
