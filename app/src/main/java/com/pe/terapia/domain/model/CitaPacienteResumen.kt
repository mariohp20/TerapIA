package com.pe.terapia.domain.model

data class CitaPacienteResumen(
    val id: String,
    val inicioEpochMillis: Long,
    val estado: EstadoCita
)