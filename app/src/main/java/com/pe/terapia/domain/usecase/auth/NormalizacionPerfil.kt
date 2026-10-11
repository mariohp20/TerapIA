package com.pe.terapia.domain.usecase.auth

import java.util.Locale

object NormalizacionPerfil {
    fun correo(value: String): String = value.trim().lowercase(Locale.ROOT)

    fun telefono(value: String): String {
        val trimmed = value.trim()
        val digits = trimmed.filter(Char::isDigit)
        return if (trimmed.startsWith('+')) "+$digits" else digits
    }

    fun dni(value: String): String = value.filter(Char::isDigit)

}
