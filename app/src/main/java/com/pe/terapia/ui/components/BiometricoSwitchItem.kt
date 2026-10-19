package com.pe.terapia.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.pe.terapia.core.biometric.BiometricPromptManager
import com.pe.terapia.core.biometric.BiometricoPreferences

@Composable
fun BiometricoSwitchItem(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val promptManager = remember(activity) { activity?.let { BiometricPromptManager(it) } }
    val prefs = remember { BiometricoPreferences(context) }
    var activo by remember { mutableStateOf(prefs.isBiometricoActivo()) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = Icons.Outlined.Fingerprint,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Acceso biométrico",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Ingresar con huella o clave del celular",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = activo,
            onCheckedChange = { activar ->
                if (activar) {
                    if (promptManager?.canAuthenticate() == true) {
                        promptManager.mostrarPromptBiometrico(
                            titulo = "Confirmar activación",
                            subtitulo = "Verifica tu identidad para habilitar esta opción",
                            onSuccess = {
                                activo = true
                                prefs.setBiometricoActivo(true)
                            },
                            onError = { /* canceló: no hacemos nada */ }
                        )
                    } else {
                        activo = false
                        prefs.setBiometricoActivo(false)
                    }
                } else {
                    activo = false
                    prefs.setBiometricoActivo(false)
                }
            }
        )
    }
}