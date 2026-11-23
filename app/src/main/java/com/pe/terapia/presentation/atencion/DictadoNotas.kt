package com.pe.terapia.presentation.atencion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pe.terapia.R

@Composable
fun AccionesDictado(
    escuchando: Boolean,
    habilitado: Boolean,
    onIniciar: () -> Unit,
    onDetener: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (escuchando) {
                FilledTonalButton(onClick = onDetener, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Stop, contentDescription = null)
                    Text(stringResource(R.string.dictado_detener))
                }
            } else {
                OutlinedButton(onClick = onIniciar, enabled = habilitado, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Mic, contentDescription = null)
                    Text(stringResource(R.string.dictado_iniciar))
                }
            }
        }
        Text(
            stringResource(R.string.dictado_privacidad),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
