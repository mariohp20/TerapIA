package com.pe.terapia.di

import com.pe.terapia.data.remote.firebase.CitaDataSource
import com.pe.terapia.data.repository.CitaRepositoryImpl
import com.pe.terapia.domain.repository.CitaRepository
import com.pe.terapia.domain.repository.PagoRepository
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerCitasHoyPsicologoUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerDisponibilidadUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerProximaCitaUseCase
import com.pe.terapia.domain.usecase.citas.ReservarCitaUseCase
import com.pe.terapia.presentation.citas.confirmacion.ConfirmacionCitaViewModel
import com.pe.terapia.presentation.citas.detalle.DetalleCitaViewModel
import com.pe.terapia.presentation.citas.reserva.ReservaCitaViewModel
import com.pe.terapia.presentation.home.paciente.inicio.HomePacienteViewModel
import com.pe.terapia.presentation.home.paciente.miscitas.MisCitasViewModel
import com.pe.terapia.presentation.home.psicologo.inicio.HomePsicologoViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val citaModule = module {
    // 1. Data Sources y Repositories
    single { CitaDataSource(get()) }
    single<CitaRepository> { CitaRepositoryImpl(get()) }

    // 2. Casos de Uso (Use Cases)
    factory { ObtenerDisponibilidadUseCase(get()) }
    factory { ObtenerProximaCitaUseCase(get()) }
    factory { ReservarCitaUseCase(get()) }
    factory { ObtenerCitasHoyPsicologoUseCase(get()) }
    // IMPORTANTE: Se eliminó ObtenerUsuarioActualUseCase porque Koin ya lo toma del AuthModule automáticamente.

    // 3. ViewModels simples
    viewModel { ReservaCitaViewModel(get()) }
    viewModel { HomePacienteViewModel(get(), get(), get()) }
    viewModel { MisCitasViewModel(get(), get()) }
    viewModel { HomePsicologoViewModel(get(), get()) }

    // 4. ViewModels con parámetros dinámicos
    viewModel { parameters ->
        ConfirmacionCitaViewModel(
            parameters[0],
            parameters[1],
            parameters[2],
            parameters[3],
            parameters[4],
            parameters[5],
            parameters[6],
            parameters[7],
            parameters[8],
            parameters[9],
            parameters[10],
            parameters[11],
            get(),
            get<ObtenerUsuarioActualUseCase>(),
            get<PagoRepository>()
        )
    }

    viewModel { parameters ->
        DetalleCitaViewModel(
            citaId = parameters[0],
            obtenerUsuarioActualUseCase = get(),
            citaRepository = get()
        )
    }
}