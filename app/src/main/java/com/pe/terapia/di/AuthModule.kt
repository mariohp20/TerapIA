package com.pe.terapia.di

import com.pe.terapia.data.remote.firebase.AuthDataSource
import com.pe.terapia.data.repository.AuthRepositoryImpl
import com.pe.terapia.domain.repository.AuthRepository
import com.pe.terapia.domain.usecase.auth.*
import org.koin.dsl.module

val authModule = module {
    single { AuthDataSource() }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    factory { LoginUseCase(get()) }
    factory { RegistroUseCase(get()) }
    factory { RecuperarPasswordUseCase(get()) }
    factory { VerificarSesionUseCase(get()) }
    // Cada uno agrega aquí su propio: viewModel { XxxViewModel(get()) }
}