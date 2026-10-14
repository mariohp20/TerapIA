package com.pe.terapia.di

import com.pe.terapia.data.remote.firebase.AdminDataSource
import com.pe.terapia.data.repository.AdminRepositoryImpl
import com.pe.terapia.domain.repository.AdminRepository
import com.pe.terapia.domain.usecase.admin.AsignarHorarioPsicologoUseCase
import com.pe.terapia.domain.usecase.admin.ObtenerEspecialidadesUseCase
import com.pe.terapia.domain.usecase.admin.ObtenerPacientesUseCase
import com.pe.terapia.domain.usecase.admin.ObtenerPsicologosUseCase
import com.pe.terapia.domain.usecase.admin.ObtenerSedesUseCase
import com.pe.terapia.domain.usecase.admin.PromoverUsuarioUseCase
import com.pe.terapia.presentation.admin.horario.AsignarHorarioViewModel
import com.pe.terapia.presentation.admin.promover.PromoverUsuarioViewModel
import com.pe.terapia.presentation.home.admin.HomeAdminViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val adminModule = module {
    single { AdminDataSource(get()) }
    single<AdminRepository> { AdminRepositoryImpl(get()) }

    // Casos de uso
    factory { PromoverUsuarioUseCase(get()) }
    factory { ObtenerPacientesUseCase(get()) }
    factory { ObtenerPsicologosUseCase(get ()) }
    factory { ObtenerSedesUseCase(get()) }
    factory { ObtenerEspecialidadesUseCase(get()) }
    factory { AsignarHorarioPsicologoUseCase(get()) }

    // ViewModels
    viewModel { HomeAdminViewModel(get()) }
    viewModel { PromoverUsuarioViewModel(get(), get(), get(), get()) }
    viewModel { AsignarHorarioViewModel(get(),get(),get()) }

}