package com.pe.terapia.domain.usecase.admin

import com.pe.terapia.domain.model.Especialidad
import com.pe.terapia.domain.model.Sede
import com.pe.terapia.domain.model.Usuario
import com.pe.terapia.domain.repository.AdminRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class DatosPromoverUsuario(
    val pacientes: List<Usuario>,
    val sedes: List<Sede>,
    val especialidades: List<Especialidad>
)

class ObtenerDatosPromoverUsuarioUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(): Result<DatosPromoverUsuario> = coroutineScope {
        val pacientesDef = async { repository.obtenerPacientes() }
        val sedesDef = async { repository.obtenerSedes() }
        val especialidadesDef = async { repository.obtenerEspecialidades() }

        val pacientes = pacientesDef.await()
        val sedes = sedesDef.await()
        val especialidades = especialidadesDef.await()

        if (pacientes.isFailure || sedes.isFailure || especialidades.isFailure) {
            Result.failure(Exception("Error al cargar los datos para promover usuarios."))
        } else {
            Result.success(
                DatosPromoverUsuario(
                    pacientes = pacientes.getOrThrow(),
                    sedes = sedes.getOrThrow(),
                    especialidades = especialidades.getOrThrow()
                )
            )
        }
    }
}