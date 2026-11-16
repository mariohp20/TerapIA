package com.pe.terapia.data.repository

import com.pe.terapia.data.local.room.NotificacionDao
import com.pe.terapia.data.local.room.NotificacionEntity
import com.pe.terapia.domain.model.NotificacionLocal
import com.pe.terapia.domain.repository.NotificacionLocalRepository
import com.pe.terapia.domain.repository.NotificacionesPreferenciasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificacionLocalRepositoryImpl(
    private val dao: NotificacionDao,
    private val preferencias: NotificacionesPreferenciasRepository
) : NotificacionLocalRepository {
    override fun observarPorUsuario(usuarioId: String): Flow<List<NotificacionLocal>> =
        dao.observarPorUsuario(usuarioId).map { lista -> lista.map { it.toDomain() } }

    override suspend fun guardar(notificacion: NotificacionLocal) {
        if (!preferencias.estanActivas(notificacion.usuarioId)) return
        dao.eliminarAnterioresA(notificacion.fechaCreacionMillis - RETENCION_MILLIS)
        dao.guardar(notificacion.toEntity())
    }

    override suspend fun marcarLeida(id: String, usuarioId: String) = dao.marcarLeida(id, usuarioId)

    override suspend fun marcarTodasLeidas(usuarioId: String) = dao.marcarTodasLeidas(usuarioId)

    private fun NotificacionEntity.toDomain() = NotificacionLocal(
        id = id,
        usuarioId = usuarioId,
        titulo = titulo,
        mensaje = mensaje,
        tipo = tipo,
        fechaCreacionMillis = fechaCreacionMillis,
        leida = leida,
        citaId = citaId
    )

    private fun NotificacionLocal.toEntity() = NotificacionEntity(
        id = id,
        usuarioId = usuarioId,
        titulo = titulo,
        mensaje = mensaje,
        tipo = tipo,
        fechaCreacionMillis = fechaCreacionMillis,
        leida = leida,
        citaId = citaId
    )

    private companion object {
        const val RETENCION_MILLIS = 180L * 24 * 60 * 60 * 1000
    }
}
