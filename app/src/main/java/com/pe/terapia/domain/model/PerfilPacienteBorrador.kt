package com.pe.terapia.domain.model

data class PerfilPacienteBorrador(
    val nombres: String = "",
    val apellidos: String = "",
    val telefono: String = "",
    val dni: String = "",
    val correo: String = "",
    val dniYaAsociado: Boolean = false
)
