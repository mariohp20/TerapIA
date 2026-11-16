package com.pe.terapia.domain.usecase.citas

import com.pe.terapia.domain.repository.CitaRepository
import com.pe.terapia.domain.model.SlotDisponible

class SolicitarGestionCitaUseCase(private val repository: CitaRepository) {
    suspend operator fun invoke(citaId: String, pacienteId: String, tipo: String, slotSolicitado: SlotDisponible? = null) =
        repository.solicitarGestionCita(citaId, pacienteId, tipo, slotSolicitado)
}

class TieneSolicitudGestionPendienteUseCase(private val repository: CitaRepository) {
    suspend operator fun invoke(citaId: String, pacienteId: String) =
        repository.tieneSolicitudGestionPendiente(citaId, pacienteId)
}

class ObtenerSolicitudGestionPendienteUseCase(private val repository: CitaRepository) {
    suspend operator fun invoke(citaId: String, pacienteId: String) =
        repository.obtenerSolicitudGestionPendiente(citaId, pacienteId)
}

class CancelarCitaPacienteUseCase(private val repository: CitaRepository) {
    suspend operator fun invoke(citaId: String, pacienteId: String) = repository.cancelarCitaPaciente(citaId, pacienteId)
}
