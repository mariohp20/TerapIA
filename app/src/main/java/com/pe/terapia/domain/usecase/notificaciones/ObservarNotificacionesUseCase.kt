package com.pe.terapia.domain.usecase.notificaciones

import com.pe.terapia.domain.model.NotificacionLocal
import com.pe.terapia.domain.model.Rol
import com.pe.terapia.domain.repository.NotificacionLocalRepository
import kotlinx.coroutines.flow.Flow

class ObservarNotificacionesUseCase(private val repository: NotificacionLocalRepository) {
    operator fun invoke(usuarioId: String): Flow<List<NotificacionLocal>> = repository.observarPorUsuario(usuarioId)
    suspend fun sincronizar(usuarioId: String, rol: Rol) = repository.sincronizar(usuarioId, rol)
}
