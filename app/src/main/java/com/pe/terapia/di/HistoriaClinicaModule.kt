package com.pe.terapia.di

import com.pe.terapia.data.remote.firebase.HistoriaClinicaDataSource
import com.pe.terapia.data.repository.HistoriaClinicaRepositoryImpl
import com.pe.terapia.domain.repository.HistoriaClinicaRepository
import com.pe.terapia.domain.usecase.historiaclinica.ObtenerAtencionesPacienteUseCase
import com.pe.terapia.domain.usecase.historiaclinica.ObtenerPacientesDePsicologoUseCase
import com.pe.terapia.domain.usecase.historiaclinica.RegistrarAtencionUseCase
import com.pe.terapia.domain.usecase.historiaclinica.ObtenerAtencionPorCitaUseCase
import com.pe.terapia.domain.usecase.historiaclinica.ObtenerEtapasPorProcesoUseCase
import com.pe.terapia.domain.usecase.historiaclinica.ObtenerProcesoActivoUseCase
import com.pe.terapia.domain.usecase.historiaclinica.ObtenerTiposEtapasUseCase
import com.pe.terapia.domain.usecase.historiaclinica.TransicionarEtapaUseCase
import com.pe.terapia.presentation.buscadorPacientes.BuscadorPacientesViewModel
import com.pe.terapia.presentation.atencion.RegistrarAtencionViewModel
import com.pe.terapia.presentation.etapas.GestionEtapasViewModel
import com.pe.terapia.presentation.historial.HistorialClinicoViewModel
import com.pe.terapia.presentation.salaconsulta.SalaConsultaViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val historiaClinicaModule = module {
    single { HistoriaClinicaDataSource(get()) }
    single<HistoriaClinicaRepository> { HistoriaClinicaRepositoryImpl(get()) }

    factory { ObtenerAtencionPorCitaUseCase (get()) }
    factory { RegistrarAtencionUseCase(get()) }
    factory { ObtenerAtencionesPacienteUseCase(get()) }
    factory { ObtenerPacientesDePsicologoUseCase(get()) }
    factory { ObtenerTiposEtapasUseCase (get()) }
    factory { TransicionarEtapaUseCase(get()) }
    factory { ObtenerEtapasPorProcesoUseCase(get()) }
    factory { ObtenerProcesoActivoUseCase(get()) }

    viewModel { RegistrarAtencionViewModel(get(), get(),get()) }
    viewModel { HistorialClinicoViewModel(get(),get(),get(), get(), get()) }
    viewModel { BuscadorPacientesViewModel(get(), get()) }
    viewModel { GestionEtapasViewModel(get(),get(),get()) }
    viewModel { SalaConsultaViewModel(get(),get(),get()) }
}