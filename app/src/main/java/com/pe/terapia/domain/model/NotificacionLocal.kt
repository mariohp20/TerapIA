package com.pe.terapia.domain.model

data class NotificacionLocal(
    val id: String,
    val usuarioId: String,
    val titulo: String,
    val mensaje: String,
    val tipo: String,
    val fechaCreacionMillis: Long,
    val leida: Boolean,
    val citaId: String? = null
)
