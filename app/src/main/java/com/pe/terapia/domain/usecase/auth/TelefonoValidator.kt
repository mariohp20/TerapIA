package com.pe.terapia.domain.usecase.auth

object TelefonoValidator {
    private val formatoEntrada = Regex("^\\+?[0-9 ().-]+$")

    fun esValido(value: String): Boolean {
        if (value.isBlank()) return true
        if (!formatoEntrada.matches(value.trim())) return false
        val canonical = NormalizacionPerfil.telefono(value)
        return canonical.count(Char::isDigit) in 7..15
    }
}
