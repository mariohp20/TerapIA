package com.pe.terapia.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "session_preferences")

class UserSessionDataStore(private val context: Context) {
    companion object {
        private val USUARIO_ROL_KEY = stringPreferencesKey("usuario_rol")
    }

    suspend fun guardarRol(rol: String) {
        context.sessionDataStore.edit { preferences ->
            preferences[USUARIO_ROL_KEY] = rol
        }
    }

    suspend fun obtenerRol(): String? {
        return context.sessionDataStore.data
            .map { preferences -> preferences[USUARIO_ROL_KEY] }
            .first()
    }

    suspend fun limpiar() {
        context.sessionDataStore.edit { preferences ->
            preferences.remove(USUARIO_ROL_KEY)
        }
    }
}
