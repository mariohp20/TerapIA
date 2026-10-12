package com.pe.terapia.domain.usecase.admin
import com.pe.terapia.domain.model.Especialidad
import com.pe.terapia.domain.repository.AdminRepository

class ObtenerEspecialidadesUseCase(private val repository: AdminRepository) {
    suspend operator fun invoke(): Result<List<Especialidad>> {
        return repository.obtenerEspecialidades()
    }
}