package com.pe.terapia.di

import com.pe.terapia.data.local.room.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val roomModule = module {
    single { AppDatabase.getDatabase(androidContext()) }
    single { get<AppDatabase>().citaDao() }
}
