package com.pe.terapia.core.qrcode

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/** Genera el bitmap Android usado para representar un código QR en la interfaz. */
class QrCodeBitmapGenerator {
    fun generar(token: String, size: Int = 512): Bitmap {
        val matrix = QRCodeWriter().encode(token, BarcodeFormat.QR_CODE, size, size)
        val bitmap = createBitmap(size, size)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap[x, y] = if (matrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        return bitmap
    }
}
