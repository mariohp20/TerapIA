package com.pe.terapia.domain.model

data class Psicologo(
    val id: String,
    val usuarioId: String,
    val nombres: String,
    val apellidos: String,
    val descripcionProfesional: String,
    val especialidadIds: List<String>,
    val sedeIds: List<String>,
    val activo: Boolean,
    val fotoUrl: String? = null
)
