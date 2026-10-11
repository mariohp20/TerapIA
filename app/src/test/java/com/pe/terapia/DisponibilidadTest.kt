package com.pe.terapia

import com.pe.terapia.domain.model.BloqueHorario
import com.pe.terapia.domain.model.ExcepcionHorario
import com.pe.terapia.domain.model.HorarioLaboral
import com.pe.terapia.domain.model.TipoExcepcionHorario
import com.pe.terapia.domain.usecase.citas.GenerarSlotsDisponibles
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DisponibilidadTest {
    private val date = LocalDate.parse("2026-10-08") // Thursday
    private val schedule = HorarioLaboral("psych~site", "psych", "site")
    private val block = BloqueHorario("thursday", 4, 9 * 60, 11 * 60)
    private val now = date.minusDays(1).atStartOfDay(ZoneId.of("America/Lima")).toInstant().toEpochMilli()

    @Test fun generatesLocalSlotsAndConvertsTheirInstantsToUtc() {
        val slots = GenerarSlotsDisponibles.generar(schedule, listOf(block), emptyList(), date, emptySet(), now)
        assertEquals(listOf("09:00", "10:00"), slots.map { it.hora })
        val expected = date.atTime(9, 0).atZone(ZoneId.of("America/Lima")).toInstant().toEpochMilli()
        assertEquals(expected, slots.first().inicioEpochMillis)
        assertEquals("2026-10-08", slots.first().fecha)
    }

    @Test fun supportsHalfHourSlotsAndKeepsLocalFieldsAlignedWithUtcInstant() {
        val halfHourBlock = block.copy(inicioMinuto = 9 * 60 + 30, finMinuto = 11 * 60 + 30)
        val slots = GenerarSlotsDisponibles.generar(schedule, listOf(halfHourBlock), emptyList(), date, emptySet(), now)
        assertEquals(listOf("09:30", "10:30"), slots.map { it.hora })
        slots.forEach { slot ->
            val local = java.time.Instant.ofEpochMilli(slot.inicioEpochMillis).atZone(ZoneId.of("America/Lima"))
            assertEquals(slot.fecha, local.toLocalDate().toString())
            assertEquals(slot.hora, local.toLocalTime().toString().take(5))
        }
    }

    @Test fun removesOccupiedAndBlockedSlotsAndAddsApprovedExtraSlot() {
        val firstSlot = date.atTime(9, 0).atZone(ZoneId.of("America/Lima")).toInstant().toEpochMilli()
        val exceptions = listOf(
            ExcepcionHorario("blocked", date, TipoExcepcionHorario.BLOQUEADO, 10 * 60),
            ExcepcionHorario("extra", date, TipoExcepcionHorario.DISPONIBILIDAD_EXTRA, 12 * 60)
        )
        val slots = GenerarSlotsDisponibles.generar(schedule, listOf(block), exceptions, date, setOf(firstSlot), now)
        assertEquals(listOf("12:00"), slots.map { it.hora })
    }

    @Test fun ignoresPastSlotsAndRejectsUnsupportedZone() {
        val past = date.atTime(9, 30).atZone(ZoneId.of("America/Lima")).toInstant().toEpochMilli()
        assertEquals(listOf("10:00"), GenerarSlotsDisponibles.generar(schedule, listOf(block), emptyList(), date, emptySet(), past).map { it.hora })
        val invalid = schedule.copy(zonaHoraria = "UTC")
        assertTrue(runCatching {
            GenerarSlotsDisponibles.generar(invalid, listOf(block), emptyList(), date, emptySet(), now)
        }.isFailure)
    }

    @Test fun rejectsUnsupportedAppointmentDuration() {
        val invalid = schedule.copy(duracionMinutos = 45)
        assertTrue(runCatching {
            GenerarSlotsDisponibles.generar(invalid, listOf(block), emptyList(), date, emptySet(), now)
        }.isFailure)
    }
}
