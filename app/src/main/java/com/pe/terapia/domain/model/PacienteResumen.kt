package com.pe.terapia.domain.model

data class PacienteResumen(
    val id: String,
    val nombreCompleto: String,
    val estado: String = "Paciente registrado"
)