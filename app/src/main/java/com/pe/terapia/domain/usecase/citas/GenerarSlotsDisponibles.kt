package com.pe.terapia.domain.usecase.citas

import com.pe.terapia.domain.model.BloqueHorario
import com.pe.terapia.domain.model.DURACION_CITA_MINUTOS
import com.pe.terapia.domain.model.ExcepcionHorario
import com.pe.terapia.domain.model.HorarioLaboral
import com.pe.terapia.domain.model.SlotDisponible
import com.pe.terapia.domain.model.TipoExcepcionHorario
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object GenerarSlotsDisponibles {
    private val horaFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun generar(
        horario: HorarioLaboral,
        bloques: List<BloqueHorario>,
        excepciones: List<ExcepcionHorario>,
        fecha: LocalDate,
        ocupados: Set<Long>,
        ahoraEpochMillis: Long = System.currentTimeMillis()
    ): List<SlotDisponible> {
        require(horario.zonaHoraria == "America/Lima") { "La zona horaria debe ser America/Lima." }
        require(horario.duracionMinutos == DURACION_CITA_MINUTOS) {
            "La duración de una cita HU02 debe ser de $DURACION_CITA_MINUTOS minutos."
        }
        if (!horario.activo) return emptyList()

        val dateExceptions = excepciones.filter { it.fecha == fecha }
        val blockedMinutes = dateExceptions
            .filter { it.tipo == TipoExcepcionHorario.BLOQUEADO }
            .map(ExcepcionHorario::inicioMinuto)
            .toSet()

        val zone = ZoneId.of(horario.zonaHoraria)
        val regular = bloques.filter { it.activo && it.diaSemana == fecha.dayOfWeek.value }.flatMap { block ->
            require(block.inicioMinuto in 0..1439 && block.finMinuto in 1..1440 && block.inicioMinuto < block.finMinuto) {
                "El bloque horario tiene un intervalo inválido."
            }
            val result = mutableListOf<SlotDisponible>()
            var minute = block.inicioMinuto
            while (minute + horario.duracionMinutos <= block.finMinuto) {
                val localDateTime = fecha.atTime(LocalTime.of(minute / 60, minute % 60))
                val instant = localDateTime.atZone(zone).toInstant()
                val epochMillis = instant.toEpochMilli()
                if (epochMillis > ahoraEpochMillis && epochMillis !in ocupados && minute !in blockedMinutes) {
                    result += SlotDisponible(
                        psicologoId = horario.psicologoId,
                        sedeId = horario.sedeId,
                        horarioId = horario.id,
                        bloqueId = block.id,
                        fecha = fecha.toString(),
                        hora = horaFormatter.format(localDateTime),
                        inicioEpochMillis = epochMillis,
                        duracionMinutos = horario.duracionMinutos
                    )
                }
                minute += horario.duracionMinutos
            }
            result
        }
        val extra = dateExceptions
            .filter { it.tipo == TipoExcepcionHorario.DISPONIBILIDAD_EXTRA }
            .mapNotNull { exception ->
                val minute = exception.inicioMinuto
                if (minute !in 0..1439) return@mapNotNull null
                val epochMillis = fecha.atTime(LocalTime.of(minute / 60, minute % 60)).atZone(zone).toInstant().toEpochMilli()
                if (epochMillis <= ahoraEpochMillis || epochMillis in ocupados || minute in blockedMinutes) return@mapNotNull null
                SlotDisponible(
                    psicologoId = horario.psicologoId,
                    sedeId = horario.sedeId,
                    horarioId = horario.id,
                    bloqueId = exception.id,
                    fecha = fecha.toString(),
                    hora = horaFormatter.format(LocalTime.of(minute / 60, minute % 60)),
                    inicioEpochMillis = epochMillis,
                    duracionMinutos = horario.duracionMinutos
                )
            }
        return (regular + extra).distinctBy(SlotDisponible::inicioEpochMillis).sortedBy(SlotDisponible::inicioEpochMillis)
    }
}
