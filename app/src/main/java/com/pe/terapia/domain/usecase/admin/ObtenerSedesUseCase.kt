package com.pe.terapia.domain.usecase.admin
import com.pe.terapia.domain.model.Sede
import com.pe.terapia.domain.repository.AdminRepository

class ObtenerSedesUseCase(private val repository: AdminRepository) {
    suspend operator fun invoke(): Result<List<Sede>> {
        return repository.obtenerSedes()
    }
}