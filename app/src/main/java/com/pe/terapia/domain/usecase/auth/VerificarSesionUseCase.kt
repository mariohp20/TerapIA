package com.pe.terapia.domain.usecase.auth

import com.pe.terapia.domain.model.Usuario
import com.pe.terapia.domain.repository.AuthRepository

class VerificarSesionUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Usuario? {
        return if (repository.haySesionActiva()) repository.obtenerUsuarioActual() else null
    }
}