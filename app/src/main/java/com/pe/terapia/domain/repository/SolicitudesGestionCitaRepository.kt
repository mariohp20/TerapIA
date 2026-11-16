package com.pe.terapia.domain.repository

import com.pe.terapia.domain.model.SolicitudGestionCita

interface SolicitudesGestionCitaRepository {
    suspend fun obtenerPendientes(): Result<List<SolicitudGestionCita>>
    suspend fun resolver(solicitudId: String, adminUid: String, decision: String): Result<Unit>
}
