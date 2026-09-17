package com.pe.terapia.domain.usecase.auth

import com.pe.terapia.domain.repository.AuthRepository

class RecuperarPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Result<Unit> {
        if (email.isBlank()) return Result.failure(IllegalArgumentException("Ingresa un correo válido."))
        return repository.recuperarPassword(email)
    }
}