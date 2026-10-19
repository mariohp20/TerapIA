package com.pe.terapia.core.pdf.Plantillas

import android.graphics.Color
import android.graphics.pdf.PdfDocument
import com.pe.terapia.core.pdf.PdfCanvas
import com.pe.terapia.core.pdf.PdfTemplate

class ComprobantePdfTemplate :
    PdfTemplate<ComprobanteData> {

    override fun render(
        document: PdfDocument,
        data: ComprobanteData
    ) {

        // A4 aproximado a 72 puntos por pulgada
        val pageInfo = PdfDocument.PageInfo
            .Builder(595, 842, 1)
            .create()

        val page = document.startPage(pageInfo)

        try {
            val pdf = PdfCanvas(page.canvas)

            pdf.text(
                "CLINICA PSICOLOGICA",
                x = 50f,
                y = 65f,
                size = 21f,
                bold = true
            )

            pdf.text(
                "COMPROBANTE DE PAGO",
                x = 50f,
                y = 105f,
                size = 16f,
                bold = true
            )

            pdf.line(
                50f, 125f,
                545f, 125f
            )

            pdf.text(
                "Nro: ${data.numero}",
                50f, 160f
            )

            pdf.text(
                "Paciente: ${data.paciente}",
                50f, 195f
            )

            pdf.text(
                "Fecha: ${data.fechaPago}",
                50f, 230f
            )

            pdf.text(
                "Concepto: ${data.concepto}",
                50f, 265f
            )

            pdf.text(
                "Metodo: ${data.metodoPago}",
                50f, 300f
            )

            pdf.text(
                "Operacion: ${data.codigoOperacion}",
                50f, 335f
            )

            pdf.line(
                50f, 370f,
                545f, 370f
            )

            pdf.text(
                "TOTAL PAGADO",
                50f, 410f,
                16f,
                true
            )

            pdf.text(
                "S/ ${data.monto}",
                380f, 410f,
                19f,
                true,
                Color.rgb(0, 120, 90)
            )

            pdf.text(
                "Documento generado electronicamente",
                50f, 770f,
                10f
            )
        } finally {
            document.finishPage(page)
        }
    }
}