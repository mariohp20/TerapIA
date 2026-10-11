package com.pe.terapia.di

import com.pe.terapia.data.remote.firebase.AuthDataSource
import com.pe.terapia.data.repository.AuthRepositoryImpl
import com.pe.terapia.domain.repository.AuthRepository
import com.pe.terapia.domain.model.Rol
import com.pe.terapia.domain.usecase.auth.CerrarSesionUseCase
import com.pe.terapia.domain.usecase.auth.CompletarPerfilUseCase
import com.pe.terapia.domain.usecase.auth.LoginConGoogleUseCase
import com.pe.terapia.domain.usecase.auth.LoginUseCase
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.auth.ObtenerBorradorPerfilUseCase
import com.pe.terapia.domain.usecase.auth.RecuperarPasswordUseCase
import com.pe.terapia.domain.usecase.auth.RegistroUseCase
import com.pe.terapia.domain.usecase.auth.VerificarSesionUseCase
import com.pe.terapia.presentation.auth.AuthViewModel
import com.pe.terapia.presentation.auth.CompletarPerfilViewModel
import com.pe.terapia.presentation.navigation.HomeAccessViewModel
import com.pe.terapia.presentation.splash.SplashViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    single { AuthDataSource(get(), get()) }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    factory { LoginUseCase(get()) }
    factory { LoginConGoogleUseCase(get()) }
    factory { RegistroUseCase(get()) }
    factory { CompletarPerfilUseCase(get()) }
    factory { CerrarSesionUseCase(get()) }
    factory { ObtenerUsuarioActualUseCase(get()) }
    factory { ObtenerBorradorPerfilUseCase(get()) }
    factory { RecuperarPasswordUseCase(get()) }
    factory { VerificarSesionUseCase(get()) }
    viewModel { AuthViewModel(get(), get(), get(), get(), get()) }
    viewModel { CompletarPerfilViewModel(get(), get(), get()) }
    viewModel { SplashViewModel(get()) }
    viewModel { (rol: Rol) -> HomeAccessViewModel(get(), rol) }
}
