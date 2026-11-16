package com.pe.terapia.data.local.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notificaciones_locales",
    indices = [Index(value = ["usuarioId", "fechaCreacionMillis"])]
)
data class NotificacionEntity(
    @PrimaryKey val id: String,
    val usuarioId: String,
    val titulo: String,
    val mensaje: String,
    val tipo: String,
    val fechaCreacionMillis: Long,
    val leida: Boolean = false,
    val citaId: String? = null,
    val alertaMostrada: Boolean = false
)
