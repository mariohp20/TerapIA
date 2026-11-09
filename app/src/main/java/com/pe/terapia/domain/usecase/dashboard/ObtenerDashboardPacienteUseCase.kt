package com.pe.terapia.domain.usecase.dashboard

import com.pe.terapia.domain.model.Cita
import com.pe.terapia.domain.model.AtencionResumenDashboard
import com.pe.terapia.domain.repository.DashboardRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class DatosDashboardPaciente(
    val atenciones: List<AtencionResumenDashboard>,
    val citas: List<Cita>
)

class ObtenerDashboardPacienteUseCase(
    private val repository: DashboardRepository
) {
    suspend operator fun invoke(pacienteId: String): Result<DatosDashboardPaciente> = coroutineScope {
        val atencionesDeferred = async { repository.obtenerAtencionesPaciente(pacienteId) }
        val citasDeferred = async { repository.obtenerCitasPorPaciente(pacienteId) }
        val atenciones = atencionesDeferred.await()
        val citas = citasDeferred.await()

        if (atenciones.isFailure) {
            Result.failure(atenciones.exceptionOrNull() ?: IllegalStateException("No se pudieron cargar las atenciones."))
        } else if (citas.isFailure) {
            Result.failure(citas.exceptionOrNull() ?: IllegalStateException("No se pudieron cargar las citas."))
        } else {
            Result.success(DatosDashboardPaciente(atenciones.getOrThrow(), citas.getOrThrow()))
        }
    }
}
