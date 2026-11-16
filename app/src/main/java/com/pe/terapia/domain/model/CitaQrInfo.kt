package com.pe.terapia.domain.model

data class CitaQrInfo(
    val citaId: String,
    val pacienteId: String,
    val pacienteNombre: String,
    val fecha: String,
    val hora: String,
    val especialidadId: String,
    val estado: String,
    val inicioEpochMillis: Long
)
