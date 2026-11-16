package com.pe.terapia.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface CitaDao {
    @Query("SELECT * FROM citas_locales WHERE pacienteId = :pacienteId AND inicioEpochMillis > :now ORDER BY inicioEpochMillis ASC LIMIT 1")
    suspend fun obtenerProximaCitaLocal(pacienteId: String, now: Long): CitaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCita(cita: CitaEntity)

    @Query("DELETE FROM citas_locales WHERE pacienteId = :pacienteId")
    suspend fun eliminarCitasPaciente(pacienteId: String)

    @Transaction
    suspend fun reemplazarProximaCita(pacienteId: String, cita: CitaEntity?) {
        eliminarCitasPaciente(pacienteId)
        if (cita != null) insertarCita(cita)
    }
}
