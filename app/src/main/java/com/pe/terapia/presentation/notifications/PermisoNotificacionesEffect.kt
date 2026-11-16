package com.pe.terapia.presentation.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/** Pide permiso una sola vez al entrar a un área autenticada con recordatorios. */
@Composable
fun PermisoNotificacionesEffect() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return@LaunchedEffect
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) return@LaunchedEffect

        val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        if (!preferences.getBoolean(KEY_PERMISSION_REQUESTED, false)) {
            preferences.edit().putBoolean(KEY_PERMISSION_REQUESTED, true).apply()
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

private const val PREFERENCES = "preferencias_recordatorios"
private const val KEY_PERMISSION_REQUESTED = "permiso_notificaciones_solicitado"
