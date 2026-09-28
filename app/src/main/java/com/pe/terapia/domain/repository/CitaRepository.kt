package com.pe.terapia.domain.repository

import com.pe.terapia.domain.model.Cita

interface CitaRepository {
    // Devuelve una lista de horas (ej. ["15:00", "16:00"]) que ya están ocupadas o disponibles
    suspend fun obtenerDisponibilidad(psicologoId: String, fecha: String): Result<List<String>>

    // Guarda la cita en la base de datos
    suspend fun reservarCita(cita: Cita): Result<Boolean>
}