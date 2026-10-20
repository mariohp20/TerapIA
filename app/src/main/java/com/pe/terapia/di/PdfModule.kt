package com.pe.terapia.di

import com.pe.terapia.core.pdf.PdfExporter
import com.pe.terapia.core.pdf.PdfGenerator
import com.pe.terapia.core.pdf.Plantillas.ComprobantePdfTemplate
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val pdfModule = module {
    single { PdfGenerator(androidContext()) }
    single { PdfExporter(androidContext()) }
    factory { ComprobantePdfTemplate() }
}
