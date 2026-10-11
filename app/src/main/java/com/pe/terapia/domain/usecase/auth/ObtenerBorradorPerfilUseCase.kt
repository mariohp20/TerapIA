package com.pe.terapia.domain.usecase.auth

import com.pe.terapia.domain.model.PerfilPacienteBorrador
import com.pe.terapia.domain.repository.AuthRepository

class ObtenerBorradorPerfilUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Result<PerfilPacienteBorrador> = repository.obtenerBorradorPerfil()
}
