package com.pe.terapia.domain.model

data class SolicitudGestionCita(
    val id: String,
    val citaId: String,
    val pacienteNombre: String,
    val tipo: String,
    val fecha: String,
    val hora: String,
    val pagada: Boolean,
    val fechaSolicitada: String? = null,
    val horaSolicitada: String? = null
)
