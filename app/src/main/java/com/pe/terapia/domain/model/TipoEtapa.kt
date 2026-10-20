package com.pe.terapia.domain.model

data class TipoEtapa(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val orden: Int,
    val activo: Boolean
)