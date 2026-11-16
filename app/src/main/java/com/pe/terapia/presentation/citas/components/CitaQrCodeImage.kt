package com.pe.terapia.presentation.citas.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.pe.terapia.core.qrcode.QrCodeBitmapGenerator

@Composable
fun CitaQrCodeImage(token: String) {
    val generadorQr = remember { QrCodeBitmapGenerator() }
    val bitmap = remember(token) { generadorQr.generar(token) }

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Código QR de la cita",
        modifier = Modifier.size(240.dp)
    )
}
