package com.pe.terapia.domain.usecase.auth

object PasswordValidator {
    fun tieneLongitudMinima(pw: String) = pw.length >= 8
    fun tieneMayuscula(pw: String) = pw.any { it.isUpperCase() }
    fun tieneNumero(pw: String) = pw.any { it.isDigit() }
    fun tieneCaracterEspecial(pw: String) = pw.any { !it.isLetterOrDigit() }

    fun esValida(pw: String) =
        tieneLongitudMinima(pw) && tieneMayuscula(pw) && tieneNumero(pw) && tieneCaracterEspecial(pw)
}