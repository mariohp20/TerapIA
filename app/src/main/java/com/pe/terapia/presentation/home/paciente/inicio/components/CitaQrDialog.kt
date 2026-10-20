package com.pe.terapia.presentation.home.paciente.inicio.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pe.terapia.domain.usecase.citas.GenerarCodigoQrCitaUseCase

@Composable
fun CitaQRCode(token: String) {
    val codigoQrUseCase = remember { GenerarCodigoQrCitaUseCase() }
    val bitmap = remember(token) { codigoQrUseCase.generarBitmapQR(token) }

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "QR de la cita",
        modifier = Modifier.size(240.dp)
    )
}

@Composable
fun CitaQrDialog(
    token: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Código QR de tu cita",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Presenta este código al llegar a tu cita",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // Fondo blanco para garantizar contraste y un escaneo correcto del QR
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CitaQRCode(token = token)
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Cerrar", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}