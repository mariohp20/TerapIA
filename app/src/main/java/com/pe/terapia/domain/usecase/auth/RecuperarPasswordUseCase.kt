package com.pe.terapia.domain.usecase.auth

import com.pe.terapia.domain.repository.AuthRepository

class RecuperarPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Result<Unit> {
        if (!EmailValidator.esValido(email)) {
            return Result.failure(IllegalArgumentException(EmailValidator.MENSAJE_INVALIDO))
        }
        return repository.recuperarPassword(NormalizacionPerfil.correo(email))
    }
}
