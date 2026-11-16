package com.pe.terapia.domain.usecase.notificaciones

import com.pe.terapia.domain.model.NotificacionLocal
import com.pe.terapia.domain.repository.NotificacionLocalRepository

class GuardarNotificacionUseCase(private val repository: NotificacionLocalRepository) {
    suspend operator fun invoke(notificacion: NotificacionLocal) = repository.guardar(notificacion)
}
