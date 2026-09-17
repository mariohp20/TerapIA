package com.pe.terapia.domain.usecase.auth

import com.pe.terapia.domain.model.Usuario
import com.pe.terapia.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<Usuario> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("El correo y la constraseña son obligatorios."))
        }
        return repository.login(email, password)
    }
}