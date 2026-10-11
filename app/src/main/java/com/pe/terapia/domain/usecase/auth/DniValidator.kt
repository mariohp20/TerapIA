package com.pe.terapia.domain.usecase.auth

object DniValidator {
    fun normalizar(value: String): String = NormalizacionPerfil.dni(value)
    fun esValido(value: String): Boolean = value.length == 8 && value.all(Char::isDigit)
}
