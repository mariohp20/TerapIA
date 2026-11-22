package com.pe.terapia.domain.repository

import com.pe.terapia.domain.model.ModoTemaApp
import kotlinx.coroutines.flow.StateFlow

interface PreferenciasTemaRepository {
    val modo: StateFlow<ModoTemaApp>
    fun establecer(modo: ModoTemaApp)
}
