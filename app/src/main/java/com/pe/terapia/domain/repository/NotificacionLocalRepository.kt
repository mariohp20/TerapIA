package com.pe.terapia.domain.repository

import com.pe.terapia.domain.model.NotificacionLocal
import com.pe.terapia.domain.model.Rol
import kotlinx.coroutines.flow.Flow

interface NotificacionLocalRepository {
    fun observarPorUsuario(usuarioId: String): Flow<List<NotificacionLocal>>
    suspend fun guardar(notificacion: NotificacionLocal)
    suspend fun sincronizar(usuarioId: String, rol: Rol)
    suspend fun marcarLeida(id: String, usuarioId: String)
    suspend fun marcarTodasLeidas(usuarioId: String)
}
