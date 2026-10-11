package com.pe.terapia.domain.model

import java.time.LocalDate

/** A date-specific exception for one concrete appointment slot. */
data class ExcepcionHorario(
    val id: String,
    val fecha: LocalDate,
    val tipo: TipoExcepcionHorario,
    val inicioMinuto: Int
)

enum class TipoExcepcionHorario { BLOQUEADO, DISPONIBILIDAD_EXTRA }
