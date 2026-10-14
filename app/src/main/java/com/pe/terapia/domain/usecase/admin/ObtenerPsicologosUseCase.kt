package com.pe.terapia.domain.usecase.admin

import com.pe.terapia.domain.model.Psicologo
import com.pe.terapia.domain.repository.AdminRepository

class ObtenerPsicologosUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(): Result<List<Psicologo>> {
        return repository.obtenerPsicologos()
    }
}