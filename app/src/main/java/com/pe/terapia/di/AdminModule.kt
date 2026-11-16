package com.pe.terapia.di

import com.pe.terapia.data.remote.firebase.AdminDataSource
import com.pe.terapia.data.repository.AdminRepositoryImpl
import com.pe.terapia.domain.repository.AdminRepository
import com.pe.terapia.domain.usecase.admin.ActualizarPsicologoUseCase
import com.pe.terapia.domain.usecase.admin.AsignarHorarioPsicologoUseCase
import com.pe.terapia.domain.usecase.admin.ObtenerDatosAsignarHorarioUseCase
import com.pe.terapia.domain.usecase.admin.ObtenerDatosGestionarPsicologoUseCase
import com.pe.terapia.domain.usecase.admin.ObtenerDatosPromoverUsuarioUseCase
import com.pe.terapia.domain.usecase.admin.PromoverUsuarioUseCase
import com.pe.terapia.presentation.admin.gestionar.GestionarPsicologoViewModel
import com.pe.terapia.presentation.admin.horario.AsignarHorarioViewModel
import com.pe.terapia.presentation.admin.promover.PromoverUsuarioViewModel
import com.pe.terapia.presentation.home.admin.HomeAdminViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val adminModule = module {
    single { AdminDataSource(get()) }
    single<AdminRepository> { AdminRepositoryImpl(get()) }

    // Promover Usuario
    factory { ObtenerDatosPromoverUsuarioUseCase(get()) }
    factory { PromoverUsuarioUseCase(get()) }

    // Asignar horario
    factory { ObtenerDatosAsignarHorarioUseCase(get ()) }
    factory { AsignarHorarioPsicologoUseCase(get()) }

    //Gestionar Psicologo
    factory { ObtenerDatosGestionarPsicologoUseCase(get()) }
    factory { ActualizarPsicologoUseCase(get()) }

    // ViewModels
    viewModel { HomeAdminViewModel(get()) }
    viewModel { PromoverUsuarioViewModel(get(), get()) }
    viewModel { AsignarHorarioViewModel(get(),get()) }
    viewModel { GestionarPsicologoViewModel(get(), get()) }
}