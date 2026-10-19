package com.pe.terapia.core.pdf

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

class PdfExporter(
    private val context: Context
) {

    fun share(file: File) {

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)

            clipData = ClipData.newRawUri(
                "PDF",
                uri
            )

            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        context.startActivity(
            Intent.createChooser(
                intent,
                "Compartir documento PDF"
            )
        )
    }

    fun saveToUri(
        file: File,
        destination: Uri
    ) {
        val output = context.contentResolver
            .openOutputStream(destination)
            ?: error("No se pudo abrir el destino")

        file.inputStream().use { input ->
            output.use { stream ->
                input.copyTo(stream)
            }
        }
    }
}