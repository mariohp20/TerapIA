package com.pe.terapia.domain.model

import java.time.Instant

data class ProcesoTerapeutico(
    val id: String,
    val historiaClinicaId: String,
    val pacienteId: String,
    val psicologoId: String,
    val estado: EstadoProceso,
    val fechaInicio: Instant,
    val fechaFin: Instant? = null,
    val motivoConsultaPrincipal: String
)