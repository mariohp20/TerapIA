package com.pe.terapia.domain.model

enum class Rol(val valor: String) {
    PACIENTE("paciente"),
    PSICOLOGO("psicologo")
}

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val rol: String = Rol.PACIENTE.valor
)