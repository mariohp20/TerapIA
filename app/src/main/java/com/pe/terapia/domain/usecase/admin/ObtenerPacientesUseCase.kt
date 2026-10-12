package com.pe.terapia.domain.usecase.admin
import com.pe.terapia.domain.model.Usuario
import com.pe.terapia.domain.repository.AdminRepository

class ObtenerPacientesUseCase(private val repository: AdminRepository) {
    suspend operator fun invoke(): Result<List<Usuario>> {
        return repository.obtenerPacientes()
    }
}