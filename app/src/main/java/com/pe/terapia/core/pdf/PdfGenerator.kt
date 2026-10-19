package com.pe.terapia.core.pdf
import android.content.Context
import android.graphics.pdf.PdfDocument
import java.io.File


interface PdfTemplate<T> {
    fun render(
        document: PdfDocument,
        data: T
    )
}



class PdfGenerator(
    private val context: Context
) {

    fun <T> generate(
        data: T,
        template: PdfTemplate<T>,
        fileName: String
    ): File {

        require(fileName.matches(
            Regex("[a-zA-Z0-9_-]+\\.pdf")
        )) {
            "Nombre de archivo inválido"
        }

        val directory = File(
            context.cacheDir,
            "pdf"
        ).apply { mkdirs() }

        val file = File(
            directory,
            fileName
        )

        val document = PdfDocument()

        try {
            template.render(document, data)

            file.outputStream().use { output ->
                document.writeTo(output)
            }

            return file

        } finally {
            document.close()
        }
    }
}