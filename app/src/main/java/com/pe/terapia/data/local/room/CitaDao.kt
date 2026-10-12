package com.pe.terapia.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CitaDao {
    @Query("SELECT * FROM citas_locales WHERE pacienteId = :pacienteId AND inicioEpochMillis > :now ORDER BY inicioEpochMillis ASC LIMIT 1")
    suspend fun obtenerProximaCitaLocal(pacienteId: String, now: Long): CitaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCita(cita: CitaEntity)
}
