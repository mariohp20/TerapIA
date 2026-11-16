package com.pe.terapia.domain.model

data class CitasPsicologoResumen(
    val pendientesResultado: List<CitaPacienteDetalle>,
    val futurasProgramadas: List<CitaPacienteDetalle>
)
