package com.pe.terapia.domain.usecase.citas

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

class GenerarCodigoQrCitaUseCase() {

    fun generarBitmapQR(
        token: String,
        size: Int = 512
    ): Bitmap {
        val matrix = QRCodeWriter().encode(
            token,
            BarcodeFormat.QR_CODE,
            size,
            size
        )

        val bitmap = createBitmap(size, size)

        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap[x, y] = if (matrix[x, y]) Color.BLACK
                else Color.WHITE
            }
        }
        return bitmap
    }

}