package com.pe.terapia.domain.usecase.auth

import com.pe.terapia.domain.model.Usuario
import com.pe.terapia.domain.repository.AuthRepository

class LoginConGoogleUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(idToken: String): Result<Usuario> {
        if (idToken.isBlank()) {
            return Result.failure(IllegalArgumentException("El token de Google es inválido."))
        }
        return repository.loginConGoogle(idToken)
    }
}