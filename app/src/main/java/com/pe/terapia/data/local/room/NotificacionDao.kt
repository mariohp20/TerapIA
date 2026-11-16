package com.pe.terapia.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificacionDao {
    @Query("SELECT * FROM notificaciones_locales WHERE usuarioId = :usuarioId ORDER BY fechaCreacionMillis DESC LIMIT 100")
    fun observarPorUsuario(usuarioId: String): Flow<List<NotificacionEntity>>

    @Query("SELECT * FROM notificaciones_locales WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): NotificacionEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarSiNoExiste(notificacion: NotificacionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(notificacion: NotificacionEntity)

    @Query("UPDATE notificaciones_locales SET leida = 1 WHERE id = :id AND usuarioId = :usuarioId")
    suspend fun marcarLeida(id: String, usuarioId: String)

    @Query("UPDATE notificaciones_locales SET leida = 1 WHERE usuarioId = :usuarioId")
    suspend fun marcarTodasLeidas(usuarioId: String)

    @Query("DELETE FROM notificaciones_locales WHERE fechaCreacionMillis < :limiteMillis")
    suspend fun eliminarAnterioresA(limiteMillis: Long)

    @Query("UPDATE notificaciones_locales SET alertaMostrada = 1 WHERE id = :id")
    suspend fun marcarAlertaMostrada(id: String)
}
