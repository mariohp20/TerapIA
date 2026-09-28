package com.pe.terapia.domain.model

data class Cita(
    val id: String = "",
    val pacienteId: String = "",
    val psicologoId: String = "",
    val fecha: String = "",
    val hora: String = "",
    val estado: String = "pendiente"
)
