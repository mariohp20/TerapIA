package com.pe.terapia.di

import com.pe.terapia.data.remote.firebase.AdminDataSource
import com.pe.terapia.data.repository.AdminRepositoryImpl
import com.pe.terapia.domain.repository.AdminRepository
import com.pe.terapia.domain.usecase.admin.ActualizarPsicologoUseCase
import com.pe.terapia.domain.usecase.admin.AsignarHorarioPsicologoUseCase
import com.pe.terapia.domain.usecase.admin.ObtenerEspecialidadesUseCase
import com.pe.terapia.domain.usecase.admin.ObtenerPacientesUseCase
import com.pe.terapia.domain.usecase.admin.ObtenerPsicologosUseCase
import com.pe.terapia.domain.usecase.admin.ObtenerSedesUseCase
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

    // Casos de uso - refactorizar para enlazarlos a los screens
    // Promover Usuario
    factory { PromoverUsuarioUseCase(get()) }
    factory { ObtenerPacientesUseCase(get()) }

    factory { ObtenerEspecialidadesUseCase(get()) }

    // Asignar horario
    factory { ObtenerPsicologosUseCase(get ()) }
    factory { AsignarHorarioPsicologoUseCase(get()) }

    //Ambos
    factory { ObtenerSedesUseCase(get()) }

    //Administrar Psicologo
    factory { ActualizarPsicologoUseCase(get()) }

    // ViewModels
    viewModel { HomeAdminViewModel(get()) }
    viewModel { PromoverUsuarioViewModel(get(), get(), get(), get()) }
    viewModel { AsignarHorarioViewModel(get(),get(),get()) }
    viewModel { GestionarPsicologoViewModel(get(), get(), get(), get()) }
}