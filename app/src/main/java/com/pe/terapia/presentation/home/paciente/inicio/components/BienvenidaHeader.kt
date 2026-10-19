package com.pe.terapia.presentation.home.paciente.inicio.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pe.terapia.R

@Composable
internal fun BienvenidaHeader(nombre: String, onCerrarSesion: () -> Unit, modifier: Modifier = Modifier) {
    val nombreMostrado = nombre.ifBlank { stringResource(R.string.inicio_paciente_generico) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        // Barra Superior
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_terapia_logo),
                    contentDescription = stringResource(R.string.splash_logo_content_description),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text("TerapIA", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Row {
                IconButton(onClick = { /* TODO: Buscar */ }) {
                    Icon(Icons.Outlined.Search, contentDescription = "Buscar")
                }
                IconButton(onClick = { /* TODO: Abrir panel de notificaciones */ }) {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Notificaciones")
                }
            }
        }

        // Saludo
        Column {
            Text(
                text = "Hola, $nombreMostrado \uD83D\uDC4B",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Tu salud mental también importa",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp
            )
        }
    }
}