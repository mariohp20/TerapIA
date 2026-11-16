package com.pe.terapia.di

import com.pe.terapia.data.local.room.AppDatabase
import com.pe.terapia.data.repository.CitaLocalRepositoryImpl
import com.pe.terapia.domain.repository.CitaLocalRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val roomModule = module {
    single { AppDatabase.getDatabase(androidContext()) }
    single { get<AppDatabase>().citaDao() }
    single<CitaLocalRepository> { CitaLocalRepositoryImpl(get()) }
}
