package com.pe.terapia.domain.repository

import kotlinx.coroutines.flow.Flow

interface NotificacionesPreferenciasRepository {
    fun observarActivas(usuarioId: String): Flow<Boolean>
    fun estanActivas(usuarioId: String): Boolean
    suspend fun establecerActivas(usuarioId: String, activas: Boolean)
}
