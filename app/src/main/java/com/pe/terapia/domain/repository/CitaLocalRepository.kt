package com.pe.terapia.domain.repository

import com.pe.terapia.domain.model.ProximaCita

interface CitaLocalRepository {
    suspend fun sincronizarProximaCita(pacienteId: String, cita: ProximaCita?): Result<Unit>
    suspend fun obtenerProximaCita(pacienteId: String, ahoraMillis: Long): Result<ProximaCita?>
}
