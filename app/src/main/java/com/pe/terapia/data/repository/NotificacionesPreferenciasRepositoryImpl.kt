package com.pe.terapia.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.pe.terapia.domain.repository.NotificacionesPreferenciasRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class NotificacionesPreferenciasRepositoryImpl(context: Context) : NotificacionesPreferenciasRepository {
    private val preferences = context.applicationContext.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observarActivas(usuarioId: String): Flow<Boolean> = callbackFlow {
        if (usuarioId.isBlank()) {
            trySend(false)
            close()
            return@callbackFlow
        }
        val key = keyFor(usuarioId)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (changedKey == key) trySend(preferences.getBoolean(key, true))
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(preferences.getBoolean(key, true))
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    override fun estanActivas(usuarioId: String): Boolean =
        usuarioId.isNotBlank() && preferences.getBoolean(keyFor(usuarioId), true)

    override suspend fun establecerActivas(usuarioId: String, activas: Boolean) {
        require(usuarioId.isNotBlank()) { "No se pudo identificar la cuenta para guardar esta preferencia." }
        preferences.edit().putBoolean(keyFor(usuarioId), activas).apply()
    }

    private fun keyFor(usuarioId: String) = "recordatorios_activos_$usuarioId"

    private companion object {
        const val PREFERENCIAS = "preferencias_notificaciones"
    }
}
