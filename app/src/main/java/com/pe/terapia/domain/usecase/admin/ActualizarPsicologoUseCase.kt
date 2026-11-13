package com.pe.terapia.domain.usecase.admin

import com.pe.terapia.domain.repository.AdminRepository

class ActualizarPsicologoUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(
        psicologoId: String,
        descripcionProfesional: String,
        especialidadIds: List<String>,
        sedeIds: List<String>
    ): Result<Unit> {
        return repository.actualizarPsicologo(
            psicologoId,
            descripcionProfesional,
            especialidadIds,
            sedeIds
        )
    }
}