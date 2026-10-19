package com.pe.terapia.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.pe.terapia.core.biometric.BiometricPromptManager
import com.pe.terapia.core.biometric.BiometricoPreferences

fun debeMostrarInvitacionBiometrica(context: Context): Boolean {
    val activity = context as? FragmentActivity ?: return false
    val prefs = BiometricoPreferences(context)
    return !prefs.isBiometricoActivo() &&
            !prefs.isModalOmitido() &&
            BiometricPromptManager(activity).canAuthenticate()
}


@Composable
fun BiometricoInvitacionHost(onFinalizar: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val promptManager = remember(activity) { activity?.let { BiometricPromptManager(it) } }
    val prefs = remember { BiometricoPreferences(context) }

    // Evita navegar dos veces (ej. cerrar el modal + callback del prompt)
    var finalizado by remember { mutableStateOf(false) }
    val finalizarUnaVez = {
        if (!finalizado) {
            finalizado = true
            onFinalizar()
        }
    }

    BiometricoInvitacionModal(
        onActivarAhora = {
            if (promptManager?.canAuthenticate() == true) {
                promptManager.mostrarPromptBiometrico(
                    titulo = "Confirmar activación",
                    subtitulo = "Verifica tu identidad para habilitar el acceso biométrico",
                    onSuccess = {
                        prefs.setBiometricoActivo(true)
                        finalizarUnaVez()
                    },
                    onError = { finalizarUnaVez() } // canceló: seguimos sin activar
                )
            } else {
                finalizarUnaVez()
            }
        },
        onMasTarde = { finalizarUnaVez() },
        onNoVolverAMostrar = {
            prefs.setModalOmitido(true)
            finalizarUnaVez()
        }
    )
}


@Composable
fun BiometricoGate(
    onDesbloqueado: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val promptManager = remember(activity) { activity?.let { BiometricPromptManager(it) } }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    val pedirAutenticacion = {
        if (promptManager == null || !promptManager.canAuthenticate()) {
            onCerrarSesion()
        } else {
            mensajeError = null
            promptManager.mostrarPromptBiometrico(
                titulo = "Desbloquear TerapIA",
                subtitulo = "Confirma tu identidad para continuar",
                onSuccess = onDesbloqueado,
                onError = { mensajeError = it }
            )
        }
    }

    // Lanza el prompt al entrar
    LaunchedEffect(Unit) { pedirAutenticacion() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Fingerprint,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "TerapIA está bloqueada",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Usa tu huella, rostro o clave del celular para continuar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        mensajeError?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = { pedirAutenticacion() }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
            Text("Desbloquear")
        }
        TextButton(onClick = onCerrarSesion) {
            Text("Cerrar sesión", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}