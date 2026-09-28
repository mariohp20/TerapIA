package com.pe.terapia.di

import org.koin.dsl.module
import com.google.firebase.firestore.FirebaseFirestore
import com.pe.terapia.data.remote.firebase.CitaDataSource
import com.pe.terapia.data.repository.CitaRepositoryImpl
import com.pe.terapia.data.repository.HistoriaClinicaRepositoryImpl
import com.pe.terapia.domain.repository.CitaRepository
import com.pe.terapia.domain.repository.HistoriaClinicaRepository
import com.pe.terapia.domain.usecase.cita.ObtenerDisponibilidadUseCase
import com.pe.terapia.domain.usecase.cita.ReservarCitaUseCase

val citaModule = module {

    single { FirebaseFirestore.getInstance() }

    single { CitaDataSource(get()) }

    single<CitaRepository> { CitaRepositoryImpl(get()) }
    single<HistoriaClinicaRepository> { HistoriaClinicaRepositoryImpl(get()) }

    factory { ObtenerDisponibilidadUseCase(get()) }
    factory { ReservarCitaUseCase(get(), get()) }

}