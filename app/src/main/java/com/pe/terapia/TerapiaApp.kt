package com.pe.terapia

import android.app.Application
import com.pe.terapia.di.adminModule
import com.pe.terapia.di.authModule
import com.pe.terapia.di.citaModule
import com.pe.terapia.di.firebaseModule
import com.pe.terapia.di.pagoModule
import com.pe.terapia.di.historiaClinicaModule
import com.pe.terapia.di.tiempoModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class TerapiaApp : Application(){
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@TerapiaApp)
            modules(listOf(firebaseModule, authModule, citaModule, pagoModule,adminModule,
                historiaClinicaModule, tiempoModule
            ))
        }
    }
}
