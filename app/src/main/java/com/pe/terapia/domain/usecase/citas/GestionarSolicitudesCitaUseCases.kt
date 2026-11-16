package com.pe.terapia.domain.usecase.citas

import com.pe.terapia.domain.repository.SolicitudesGestionCitaRepository

class ObtenerSolicitudesGestionCitaUseCase(private val repository: SolicitudesGestionCitaRepository) {
    suspend operator fun invoke() = repository.obtenerPendientes()
}

class ResolverSolicitudGestionCitaUseCase(private val repository: SolicitudesGestionCitaRepository) {
    suspend operator fun invoke(solicitudId: String, adminUid: String, decision: String) =
        repository.resolver(solicitudId, adminUid, decision)
}
