package com.pe.terapia

import android.app.Application
import com.pe.terapia.di.authModule
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
            modules(listOf(authModule))
        }
    }
}