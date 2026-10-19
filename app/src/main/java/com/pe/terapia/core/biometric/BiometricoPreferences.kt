package com.pe.terapia.core.biometric

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class BiometricoPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("seguridad_terapia", Context.MODE_PRIVATE)

    // Saber si la huella esta activa
    fun isBiometricoActivo(): Boolean = prefs.getBoolean("huella_activa", false)
    fun setBiometricoActivo(activo: Boolean) = prefs.edit { putBoolean("huella_activa", activo) }

    // Saber si el usuario ya dijo "No volver a preguntar"
    fun isModalOmitido(): Boolean = prefs.getBoolean("modal_omitido", false)
    fun setModalOmitido(omitido: Boolean) = prefs.edit { putBoolean("modal_omitido", omitido) }
}
