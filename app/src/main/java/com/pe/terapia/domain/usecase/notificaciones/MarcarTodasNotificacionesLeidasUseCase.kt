package com.pe.terapia.domain.usecase.notificaciones

import com.pe.terapia.domain.repository.NotificacionLocalRepository

class MarcarTodasNotificacionesLeidasUseCase(private val repository: NotificacionLocalRepository) {
    suspend operator fun invoke(usuarioId: String) = repository.marcarTodasLeidas(usuarioId)
}
