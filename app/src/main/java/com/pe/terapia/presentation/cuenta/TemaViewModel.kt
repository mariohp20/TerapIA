package com.pe.terapia.presentation.cuenta

import androidx.lifecycle.ViewModel
import com.pe.terapia.domain.model.ModoTemaApp
import com.pe.terapia.domain.repository.PreferenciasTemaRepository

class TemaViewModel(private val preferencias: PreferenciasTemaRepository) : ViewModel() {
    val modo = preferencias.modo
    fun establecer(modo: ModoTemaApp) = preferencias.establecer(modo)
}
