package com.pe.terapia.domain.model

enum class EstadoReserva {
    PENDIENTE,
    CONFIRMADA,
    EXPIRADA,
    CANCELADA
}

data class Reserva(
    val id: String = "",
    val pacienteId: String = "",
    val psicologoId: String = "",
    val sedeId: String = "",
    val horarioId: String = "",
    val bloqueId: String = "",
    val especialidadId: String = "",
    val fecha: String = "",
    val hora: String = "",
    val inicioEpochMillis: Long = 0,
    val duracionMinutos: Int = DURACION_CITA_MINUTOS,
    val estado: EstadoReserva = EstadoReserva.PENDIENTE
)
