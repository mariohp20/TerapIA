package com.pe.terapia.domain.model

data class HorarioLaboral(
    val id: String,
    val psicologoId: String,
    val sedeId: String,
    val zonaHoraria: String = "America/Lima",
    val duracionMinutos: Int = DURACION_CITA_MINUTOS,
    val activo: Boolean = true
)

data class BloqueHorario(
    val id: String,
    /** ISO day of week: Monday = 1 through Sunday = 7. */
    val diaSemana: Int,
    val inicioMinuto: Int,
    val finMinuto: Int,
    val activo: Boolean = true
)
