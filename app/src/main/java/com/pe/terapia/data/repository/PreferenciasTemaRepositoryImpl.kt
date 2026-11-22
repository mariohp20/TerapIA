package com.pe.terapia.data.repository

import android.content.Context
import com.pe.terapia.domain.model.ModoTemaApp
import com.pe.terapia.domain.repository.PreferenciasTemaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferenciasTemaRepositoryImpl(context: Context) : PreferenciasTemaRepository {
    private val preferences = context.getSharedPreferences("preferencias_apariencia", Context.MODE_PRIVATE)
    private val _modo = MutableStateFlow(
        preferences.getString(KEY_MODO, null)?.let { runCatching { ModoTemaApp.valueOf(it) }.getOrNull() }
            ?: ModoTemaApp.SISTEMA
    )
    override val modo = _modo.asStateFlow()

    override fun establecer(modo: ModoTemaApp) {
        preferences.edit().putString(KEY_MODO, modo.name).apply()
        _modo.value = modo
    }

    private companion object { const val KEY_MODO = "modo_tema" }
}
