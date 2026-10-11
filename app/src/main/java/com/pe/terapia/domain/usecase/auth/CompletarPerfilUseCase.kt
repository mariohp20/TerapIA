package com.pe.terapia.domain.usecase.auth

import com.pe.terapia.domain.model.Usuario
import com.pe.terapia.domain.repository.AuthRepository

class CompletarPerfilUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        nombres: String,
        apellidos: String,
        telefono: String?,
        dni: String
    ): Result<Usuario> {
        if (nombres.isBlank() || apellidos.isBlank()) {
            return Result.failure(IllegalArgumentException("Nombres, apellidos y DNI son obligatorios."))
        }
        val normalizedDni = DniValidator.normalizar(dni)
        if (!DniValidator.esValido(normalizedDni)) {
            return Result.failure(IllegalArgumentException("El DNI debe tener exactamente ocho dígitos."))
        }
        if (!telefono.isNullOrBlank() && !TelefonoValidator.esValido(telefono)) {
            return Result.failure(IllegalArgumentException("Ingresa un teléfono válido."))
        }
        return repository.completarPerfil(
            nombres.trim(), apellidos.trim(),
            telefono?.takeIf { it.isNotBlank() }?.let(NormalizacionPerfil::telefono), normalizedDni
        )
    }
}
