package com.pe.terapia.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "citas_locales")
data class CitaEntity(
    @PrimaryKey val id: String,
    val pacienteId: String,
    val psicologoNombre: String,
    val especialidadNombre: String,
    val sedeNombre: String,
    val sedeDireccion: String,
    val fecha: String,
    val hora: String,
    val inicioEpochMillis: Long
)
