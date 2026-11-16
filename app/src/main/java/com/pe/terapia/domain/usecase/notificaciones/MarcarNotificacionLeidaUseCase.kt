package com.pe.terapia.domain.usecase.notificaciones

import com.pe.terapia.domain.repository.NotificacionLocalRepository

class MarcarNotificacionLeidaUseCase(private val repository: NotificacionLocalRepository) {
    suspend operator fun invoke(id: String, usuarioId: String) = repository.marcarLeida(id, usuarioId)
}
