package com.pe.terapia.di

import com.pe.terapia.data.remote.firebase.AdminDataSource
import com.pe.terapia.data.repository.AdminRepositoryImpl
import com.pe.terapia.domain.repository.AdminRepository
import com.pe.terapia.domain.usecase.admin.ObtenerEspecialidadesUseCase
import com.pe.terapia.domain.usecase.admin.ObtenerPacientesUseCase
import com.pe.terapia.domain.usecase.admin.ObtenerSedesUseCase
import com.pe.terapia.domain.usecase.admin.PromoverUsuarioUseCase
import com.pe.terapia.presentation.home.admin.PromoverUsuarioViewModel
import com.pe.terapia.presentation.home.admin.HomeAdminViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val adminModule = module {
    single { AdminDataSource(get()) }
    single<AdminRepository> { AdminRepositoryImpl(get()) }

    // Casos de uso
    factory { PromoverUsuarioUseCase(get()) }
    factory { ObtenerPacientesUseCase(get()) }
    factory { ObtenerSedesUseCase(get()) }
    factory { ObtenerEspecialidadesUseCase(get()) }

    // ViewModels
    viewModel { HomeAdminViewModel(get()) }
    viewModel { PromoverUsuarioViewModel(get(), get(), get(), get()) }
}