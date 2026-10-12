package com.pe.terapia.domain.model

import java.time.Instant

data class HistoriaClinica(
    val id: String = "",
    val pacienteId: String,
    val fechaCreacion: Instant,
    val fechaActualizacion: Instant,
    val activo: Boolean = true
)