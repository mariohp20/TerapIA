package com.pe.terapia.di

import com.pe.terapia.data.remote.firebase.DashboardDataSource
import com.pe.terapia.data.repository.DashboardRepositoryImpl
import com.pe.terapia.domain.repository.DashboardRepository
import com.pe.terapia.domain.usecase.dashboard.ObtenerAtencionesPsicologoUseCase
import com.pe.terapia.domain.usecase.dashboard.ObtenerCitasPsicologoUseCase
import com.pe.terapia.domain.usecase.dashboard.ObtenerEtapasActivasPsicologoUseCase
import com.pe.terapia.presentation.dashboard.DashboardPsicologoViewModel
import com.pe.terapia.domain.usecase.dashboard.ObtenerDashboardPacienteUseCase
import com.pe.terapia.presentation.dashboard.DashboardPacienteViewModel
import com.pe.terapia.domain.usecase.dashboard.ObtenerDashboardAdminUseCase
import com.pe.terapia.presentation.dashboard.DashboardAdminViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val dashboardModule = module {

    single { DashboardDataSource(get()) }
    single<DashboardRepository> { DashboardRepositoryImpl(get()) }

    // Psicologo
    factory { ObtenerAtencionesPsicologoUseCase(get()) }
    factory { ObtenerEtapasActivasPsicologoUseCase(get()) }
    factory { ObtenerCitasPsicologoUseCase(get()) }
    factory { ObtenerDashboardPacienteUseCase(get()) }
    factory { ObtenerDashboardAdminUseCase(get()) }

    // Psicologo
    viewModel {DashboardPsicologoViewModel(get(),get(),get(),get(),get()) }
    viewModel { DashboardPacienteViewModel(get(), get()) }
    viewModel { DashboardAdminViewModel(get()) }
}
