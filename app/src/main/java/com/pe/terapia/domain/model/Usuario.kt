package com.pe.terapia.domain.model

enum class Rol(val valor: String) {
    PACIENTE("paciente"),
    PSICOLOGO("psicologo"),
    ADMIN("admin");

    companion object {
        fun fromValue(value: String?): Rol? = entries.firstOrNull { it.valor == value }
    }
}

data class Usuario(
    val uid: String,
    val nombres: String,
    val apellidos: String,
    val correo: String,
    val telefono: String?,
    val rol: Rol,
    val activo: Boolean,
    val fechaRegistro: java.time.Instant,
    val fotoUrl: String? = null,
    /** DNI declarado por el paciente; no representa una identidad verificada. */
    val dni: String? = null
) {
    val nombreCompleto: String get() = "$nombres $apellidos".trim()
}
