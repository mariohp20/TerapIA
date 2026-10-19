package com.pe.terapia

import android.app.Application
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.pe.terapia.di.adminModule
import com.pe.terapia.di.authModule
import com.pe.terapia.di.citaModule
import com.pe.terapia.di.firebaseModule
import com.pe.terapia.di.pagoModule
import com.pe.terapia.di.historiaClinicaModule
import com.pe.terapia.di.roomModule
import com.pe.terapia.di.pdfModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class TerapiaApp : Application(){
    override fun onCreate() {
        super.onCreate()

        // Inicializar Firebase App Check (Debug en desarrollo, Play Integrity en producción)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            if (com.pe.terapia.BuildConfig.DEBUG) {
                DebugAppCheckProviderFactory.getInstance()
            } else {
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }
        )

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@TerapiaApp)
            modules(listOf(firebaseModule, authModule, citaModule, pagoModule, adminModule,
                historiaClinicaModule, roomModule, pdfModule
            ))
        }
    }
}
