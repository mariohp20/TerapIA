package com.pe.terapia.di

import com.pe.terapia.data.remote.firebase.HistoriaClinicaDataSource
import com.pe.terapia.data.repository.HistoriaClinicaRepositoryImpl
import com.pe.terapia.domain.repository.HistoriaClinicaRepository
import com.pe.terapia.domain.usecase.historiaclinica.RegistrarAtencionUseCase
import org.koin.dsl.module

val historiaClinicaModule = module {
    single { HistoriaClinicaDataSource(get()) }
    single<HistoriaClinicaRepository> { HistoriaClinicaRepositoryImpl(get()) }
    factory { RegistrarAtencionUseCase(get()) }
}