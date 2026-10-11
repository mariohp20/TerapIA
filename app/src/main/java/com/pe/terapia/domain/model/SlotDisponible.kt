package com.pe.terapia.domain.model

data class SlotDisponible(
    val psicologoId: String,
    val sedeId: String,
    val horarioId: String,
    val bloqueId: String,
    val fecha: String,
    val hora: String,
    val inicioEpochMillis: Long,
    val duracionMinutos: Int = DURACION_CITA_MINUTOS
)
