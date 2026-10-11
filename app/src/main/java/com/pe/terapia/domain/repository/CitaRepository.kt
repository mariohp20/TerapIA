package com.pe.terapia.domain.repository

import com.pe.terapia.domain.model.Cita
import com.pe.terapia.domain.model.Especialidad
import com.pe.terapia.domain.model.Reserva
import com.pe.terapia.domain.model.Sede
import com.pe.terapia.domain.model.SlotDisponible
import com.pe.terapia.domain.model.Psicologo
import com.pe.terapia.domain.model.ProximaCita

interface CitaRepository {
    suspend fun obtenerEspecialidades(): Result<List<Especialidad>>
    suspend fun obtenerPsicologosPorEspecialidad(especialidadId: String): Result<List<Psicologo>>
    suspend fun obtenerSedes(psicologoId: String): Result<List<Sede>>
    suspend fun obtenerDisponibilidad(psicologoId: String, sedeId: String, fecha: String): Result<List<SlotDisponible>>
    suspend fun obtenerProximaCita(pacienteId: String): Result<ProximaCita?>
    suspend fun reservarYConfirmar(reserva: Reserva): Result<Cita>
}
