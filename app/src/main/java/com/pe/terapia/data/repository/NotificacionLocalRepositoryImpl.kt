package com.pe.terapia.data.repository

import com.pe.terapia.data.local.room.NotificacionDao
import com.pe.terapia.data.local.room.NotificacionEntity
import com.pe.terapia.domain.model.NotificacionLocal
import com.pe.terapia.domain.model.Rol
import com.pe.terapia.domain.repository.NotificacionLocalRepository
import com.pe.terapia.domain.repository.NotificacionesPreferenciasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

class NotificacionLocalRepositoryImpl(
    private val dao: NotificacionDao,
    private val preferencias: NotificacionesPreferenciasRepository,
    private val firestore: FirebaseFirestore
) : NotificacionLocalRepository {
    override fun observarPorUsuario(usuarioId: String): Flow<List<NotificacionLocal>> =
        dao.observarPorUsuario(usuarioId).map { lista -> lista.map { it.toDomain() } }

    override suspend fun guardar(notificacion: NotificacionLocal) {
        if (!preferencias.estanActivas(notificacion.usuarioId)) return
        // Eventos con ID estable son idempotentes: una repetición del flujo no
        // vuelve a insertar ni reabre una notificación ya leída.
        if (dao.obtenerPorId(notificacion.id) != null) return
        dao.eliminarAnterioresA(notificacion.fechaCreacionMillis - RETENCION_MILLIS)
        dao.insertarSiNoExiste(notificacion.toEntity())
    }

    override suspend fun sincronizar(usuarioId: String, rol: Rol) {
        if (usuarioId.isBlank()) return
        val query = if (rol == Rol.ADMIN) {
            firestore.collection("notificaciones").whereEqualTo("destinatarioRol", Rol.ADMIN.valor)
        } else {
            firestore.collection("notificaciones").whereEqualTo("usuarioId", usuarioId)
        }
        val snapshot = query.get().await()
        val now = System.currentTimeMillis()
        dao.eliminarAnterioresA(now - RETENCION_MILLIS)
        snapshot.documents.forEach { document ->
            val createdAt = when (val value = document.get("fechaCreacion")) {
                is Timestamp -> value.toDate().time
                is Number -> value.toLong()
                else -> now
            }
            val owner = if (rol == Rol.ADMIN) usuarioId else document.getString("usuarioId") ?: return@forEach
            dao.insertarSiNoExiste(
                NotificacionEntity(
                    id = document.id,
                    usuarioId = owner,
                    titulo = document.getString("titulo").orEmpty().ifBlank { "Aviso de TerapIA" },
                    mensaje = document.getString("mensaje").orEmpty(),
                    tipo = document.getString("tipo").orEmpty(),
                    fechaCreacionMillis = createdAt,
                    citaId = document.getString("citaId")
                )
            )
        }
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
