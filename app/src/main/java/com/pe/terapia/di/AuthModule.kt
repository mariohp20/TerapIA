package com.pe.terapia.di

import com.pe.terapia.data.remote.firebase.AuthDataSource
import com.pe.terapia.data.repository.AuthRepositoryImpl
import com.pe.terapia.domain.repository.AuthRepository
import com.pe.terapia.domain.usecase.auth.*
import com.pe.terapia.presentation.auth.AuthViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    single { AuthDataSource() }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    factory { LoginUseCase(get()) }
    factory { LoginConGoogleUseCase(get()) }
    factory { RegistroUseCase(get()) }
    factory { RecuperarPasswordUseCase(get()) }
    factory { VerificarSesionUseCase(get()) }
    viewModel { AuthViewModel(get(), get(),get()) }
}