package com.pe.terapia.domain.model

data class ProximaCita(
    val id: String = "",
    val psicologoNombre: String,
    val especialidadNombre: String,
    val fecha: String,
    val hora: String,
    val sedeNombre: String,
    val sedeDireccion: String
)
