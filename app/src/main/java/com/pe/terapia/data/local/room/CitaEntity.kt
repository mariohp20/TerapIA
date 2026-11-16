package com.pe.terapia.data.local.room

import androidx.room.Entity
import androidx.room.ColumnInfo
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
    val inicioEpochMillis: Long,
    val estado: String = "",
    val estadoPago: String = "PENDIENTE",
    @ColumnInfo(defaultValue = "0.0") val sedeLatitud: Double = 0.0,
    @ColumnInfo(defaultValue = "0.0") val sedeLongitud: Double = 0.0,
    @ColumnInfo(defaultValue = "0") val pagado: Boolean = false
)
