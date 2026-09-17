package com.pe.terapia.domain.usecase.auth

import com.pe.terapia.domain.model.Usuario
import com.pe.terapia.domain.repository.AuthRepository

class RegistroUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, nombre: String): Result<Usuario> {
        if (email.isBlank() || password.isBlank() || nombre.isBlank()) {
            return Result.failure(IllegalArgumentException("Todos los campos son obligatorios."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("La contraseña debe tener al menos 6 caracteres."))
        }
        return repository.registro(email, password, nombre)
    }
}