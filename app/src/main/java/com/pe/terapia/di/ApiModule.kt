package com.pe.terapia.di

import com.pe.terapia.data.remote.api.FeriadosApi
import com.pe.terapia.data.repository.FeriadosRepositoryImpl
import com.pe.terapia.domain.repository.FeriadosRepository
import com.pe.terapia.domain.usecase.citas.ObtenerFeriadosUseCase
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val apiModule = module {
    single {
        Retrofit.Builder()
            .baseUrl("https://date.nager.at/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    single<FeriadosApi> { get<Retrofit>().create(FeriadosApi::class.java) }
    single<FeriadosRepository> { FeriadosRepositoryImpl(get()) }
    factory { ObtenerFeriadosUseCase(get()) }
}
