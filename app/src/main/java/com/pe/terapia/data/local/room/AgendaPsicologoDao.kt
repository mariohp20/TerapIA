package com.pe.terapia.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface AgendaPsicologoDao {
    @Query("SELECT * FROM agenda_psicologo_local WHERE psicologoId = :psicologoId AND fecha = :fecha ORDER BY inicioEpochMillis")
    suspend fun obtener(psicologoId: String, fecha: String): List<AgendaPsicologoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(citas: List<AgendaPsicologoEntity>)

    @Query("DELETE FROM agenda_psicologo_local WHERE psicologoId = :psicologoId AND fecha = :fecha")
    suspend fun eliminar(psicologoId: String, fecha: String)

    @Transaction
    suspend fun reemplazar(psicologoId: String, fecha: String, citas: List<AgendaPsicologoEntity>) {
        eliminar(psicologoId, fecha)
        if (citas.isNotEmpty()) insertarTodas(citas)
    }
}
