package com.pe.terapia.di

import com.pe.terapia.data.remote.firebase.CitaDataSource
import com.pe.terapia.data.repository.CitaRepositoryImpl
import com.pe.terapia.domain.repository.CitaRepository
import com.pe.terapia.domain.repository.HistoriaClinicaRepository
import com.pe.terapia.domain.repository.PagoRepository
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerCitasHoyPsicologoUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerDisponibilidadUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerProximaCitaUseCase
import com.pe.terapia.domain.usecase.citas.ReservarCitaUseCase
import com.pe.terapia.presentation.citas.confirmacion.ConfirmacionCitaViewModel
import com.pe.terapia.presentation.citas.reserva.ReservaCitaViewModel
import com.pe.terapia.presentation.home.paciente.HomePacienteViewModel
import com.pe.terapia.presentation.home.psicologo.HomePsicologoViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val citaModule = module {
    single { CitaDataSource(get()) }
    single<CitaRepository> { CitaRepositoryImpl(get()) }
    factory { ObtenerDisponibilidadUseCase(get()) }
    factory { ObtenerProximaCitaUseCase(get()) }
    factory { ReservarCitaUseCase(get()) }
    factory { ObtenerCitasHoyPsicologoUseCase(get()) }
    factory { ObtenerUsuarioActualUseCase(get()) }

    viewModel { ReservaCitaViewModel(get()) }
    viewModel { HomePacienteViewModel(get(), get(), get()) }
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
            get(),
            get<ObtenerUsuarioActualUseCase>(),
            get<PagoRepository>()
        )
    }
    viewModel { HomePsicologoViewModel(get(), get()) }
}
