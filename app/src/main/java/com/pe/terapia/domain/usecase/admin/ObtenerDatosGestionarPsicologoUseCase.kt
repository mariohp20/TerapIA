package com.pe.terapia.domain.usecase.admin

import com.pe.terapia.domain.model.Especialidad
import com.pe.terapia.domain.model.Psicologo
import com.pe.terapia.domain.model.Sede
import com.pe.terapia.domain.repository.AdminRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

data class DatosGestionarPsicologo(
    val psicologos: List<Psicologo>,
    val sedes: List<Sede>,
    val especialidades: List<Especialidad>
)

class ObtenerDatosGestionarPsicologoUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(): Result<DatosGestionarPsicologo> = coroutineScope {
        val psicologosDef = async { repository.obtenerPsicologos() }
        val sedesDef = async { repository.obtenerSedes() }
        val especialidadesDef = async { repository.obtenerEspecialidades() }

        val psicologos = psicologosDef.await()
        val sedes = sedesDef.await()
        val especialidades = especialidadesDef.await()

        psicologos.exceptionOrNull()?.let { error ->
            return@coroutineScope Result.failure(
                IllegalStateException("No se pudieron cargar los psicólogos: ${error.message.orEmpty()}", error)
            )
        }
        sedes.exceptionOrNull()?.let { error ->
            return@coroutineScope Result.failure(
                IllegalStateException("No se pudieron cargar las sedes: ${error.message.orEmpty()}", error)
            )
        }
        especialidades.exceptionOrNull()?.let { error ->
            return@coroutineScope Result.failure(
                IllegalStateException("No se pudieron cargar las especialidades: ${error.message.orEmpty()}", error)
            )
        }

        Result.success(
            DatosGestionarPsicologo(
                psicologos = psicologos.getOrThrow(),
                sedes = sedes.getOrThrow(),
                especialidades = especialidades.getOrThrow()
            )
        )
    }
}
