package com.pe.terapia.domain.usecase.admin

import com.pe.terapia.domain.model.Psicologo
import com.pe.terapia.domain.model.Sede
import com.pe.terapia.domain.repository.AdminRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class DatosAsignarHorario(
    val psicologos: List<Psicologo>,
    val sedes: List<Sede>
)

class ObtenerDatosAsignarHorarioUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(): Result<DatosAsignarHorario> = coroutineScope {
        val psicologosDef = async { repository.obtenerPsicologos() }
        val sedesDef = async { repository.obtenerSedes() }

        val psicologos = psicologosDef.await()
        val sedes = sedesDef.await()

        if (psicologos.isFailure || sedes.isFailure) {
            Result.failure(Exception("Error al cargar los datos para asignar horarios."))
        } else {
            Result.success(
                DatosAsignarHorario(
                    psicologos = psicologos.getOrThrow(),
                    sedes = sedes.getOrThrow()
                )
            )
        }
    }
}