package com.pe.terapia.domain.model

import java.time.Instant

data class Atencion(
    val id: String = "",
    val citaId: String,
    val pacienteId: String,
    val psicologoId: String,
    val historiaClinicaId: String,
    val fechaAtencion: Instant,
    val motivoConsulta: String = "",
    val observaciones: String = "",
    val evaluacion: String = "",
    val evolucion: String = "",
    val indicaciones: String = ""
)