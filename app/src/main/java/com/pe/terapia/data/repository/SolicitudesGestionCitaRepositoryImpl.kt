package com.pe.terapia.data.repository

import com.pe.terapia.data.remote.firebase.SolicitudesGestionCitaDataSource
import com.pe.terapia.domain.model.SolicitudGestionCita
import com.pe.terapia.domain.repository.SolicitudesGestionCitaRepository
import kotlinx.coroutines.CancellationException

class SolicitudesGestionCitaRepositoryImpl(private val source: SolicitudesGestionCitaDataSource) : SolicitudesGestionCitaRepository {
    override suspend fun obtenerPendientes(): Result<List<SolicitudGestionCita>> = capture { source.obtenerPendientes() }
    override suspend fun resolver(solicitudId: String, adminUid: String, decision: String): Result<Unit> = capture {
        source.resolver(solicitudId, adminUid, decision)
    }

    private suspend fun <T> capture(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (cancel: CancellationException) {
        throw cancel
    } catch (error: Exception) {
        Result.failure(error)
    }
}
