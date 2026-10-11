package com.pe.terapia.domain.usecase.auth

import com.pe.terapia.domain.model.Usuario
import com.pe.terapia.domain.repository.AuthRepository

class RegistroUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        nombres: String,
        apellidos: String,
        email: String,
        password: String,
        telefono: String?,
        dni: String
    ): Result<Usuario> {
        if (nombres.isBlank() || apellidos.isBlank() || email.isBlank() || password.isBlank() || dni.isBlank()) {
            return Result.failure(IllegalArgumentException("Nombres, apellidos, correo, contraseña y DNI son obligatorios."))
        }
        if (!EmailValidator.esValido(email)) {
            return Result.failure(IllegalArgumentException(EmailValidator.MENSAJE_INVALIDO))
        }
        if (!PasswordValidator.esValida(password)) {
            return Result.failure(IllegalArgumentException("La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un carácter especial."))
        }
        if (!telefono.isNullOrBlank() && !TelefonoValidator.esValido(telefono)) {
            return Result.failure(IllegalArgumentException("Ingresa un teléfono válido."))
        }
        val normalizedDni = DniValidator.normalizar(dni)
        if (!DniValidator.esValido(normalizedDni)) {
            return Result.failure(IllegalArgumentException("El DNI debe tener exactamente ocho dígitos."))
        }
        return repository.registro(
            nombres.trim(), apellidos.trim(), NormalizacionPerfil.correo(email), password,
            telefono?.takeIf(String::isNotBlank)?.let(NormalizacionPerfil::telefono), normalizedDni
        )
    }
}
