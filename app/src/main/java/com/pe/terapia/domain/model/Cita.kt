package com.pe.terapia.domain.model

enum class EstadoCita {
    PROGRAMADA,
    COMPLETADA,
    CANCELADA,
    NO_ASISTIO
}

data class Cita(
    val id: String = "",
    val reservaId: String = "",
    val pacienteId: String = "",
    val psicologoId: String = "",
    val sedeId: String = "",
    val especialidadId: String = "",
    val fecha: String = "",
    val hora: String = "",
    val inicioEpochMillis: Long = 0,
    val duracionMinutos: Int = DURACION_CITA_MINUTOS,
    val estado: EstadoCita = EstadoCita.PROGRAMADA
)

const val DURACION_CITA_MINUTOS = 60
