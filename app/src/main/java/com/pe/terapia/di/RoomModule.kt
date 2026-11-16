package com.pe.terapia.di

import com.pe.terapia.data.local.room.AppDatabase
import com.pe.terapia.data.repository.CitaLocalRepositoryImpl
import com.pe.terapia.data.repository.NotificacionLocalRepositoryImpl
import com.pe.terapia.data.repository.NotificacionesPreferenciasRepositoryImpl
import com.pe.terapia.data.repository.AgendaPsicologoLocalRepositoryImpl
import com.pe.terapia.domain.repository.CitaLocalRepository
import com.pe.terapia.domain.repository.NotificacionLocalRepository
import com.pe.terapia.domain.repository.NotificacionesPreferenciasRepository
import com.pe.terapia.domain.repository.AgendaPsicologoLocalRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val roomModule = module {
    single { AppDatabase.getDatabase(androidContext()) }
    single { get<AppDatabase>().citaDao() }
    single<CitaLocalRepository> { CitaLocalRepositoryImpl(get()) }
    single { get<AppDatabase>().notificacionDao() }
    single { get<AppDatabase>().agendaPsicologoDao() }
    single<AgendaPsicologoLocalRepository> { AgendaPsicologoLocalRepositoryImpl(get()) }
    single<NotificacionesPreferenciasRepository> { NotificacionesPreferenciasRepositoryImpl(androidContext()) }
    single<NotificacionLocalRepository> { NotificacionLocalRepositoryImpl(get(), get()) }
}
