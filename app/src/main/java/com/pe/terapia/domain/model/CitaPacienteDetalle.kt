package com.pe.terapia.domain.model

data class CitaPacienteDetalle(
    val citaId: String,
    val pacienteId: String,
    val hora: String,
    val nombrePaciente: String
)