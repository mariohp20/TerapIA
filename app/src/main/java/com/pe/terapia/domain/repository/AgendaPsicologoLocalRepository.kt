package com.pe.terapia.domain.repository

import com.pe.terapia.domain.model.CitaPacienteDetalle

interface AgendaPsicologoLocalRepository {
    suspend fun obtener(psicologoId: String, fecha: String): List<CitaPacienteDetalle>
    suspend fun guardar(psicologoId: String, fecha: String, citas: List<CitaPacienteDetalle>)
}
