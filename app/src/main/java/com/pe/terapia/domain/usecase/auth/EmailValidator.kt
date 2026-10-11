package com.pe.terapia.domain.usecase.auth

object EmailValidator {
    const val MENSAJE_INVALIDO = "Ingresa un correo válido."

    private val formatoBasico = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun esValido(email: String): Boolean = formatoBasico.matches(email.trim())
}
