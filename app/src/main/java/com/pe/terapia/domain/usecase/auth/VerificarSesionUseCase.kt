package com.pe.terapia.domain.usecase.auth

import com.pe.terapia.domain.model.Usuario
import com.pe.terapia.domain.repository.AuthRepository

class VerificarSesionUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Result<Usuario?> = repository.obtenerUsuarioActual()
}
