package com.pe.terapia.di

import com.pe.terapia.data.remote.firebase.CitaDataSource
import com.pe.terapia.data.remote.firebase.SolicitudesGestionCitaDataSource
import com.pe.terapia.data.repository.CitaRepositoryImpl
import com.pe.terapia.data.repository.SolicitudesGestionCitaRepositoryImpl
import com.pe.terapia.data.work.WorkManagerRecordatorioCitasScheduler
import com.pe.terapia.domain.repository.CitaRepository
import com.pe.terapia.domain.repository.SolicitudesGestionCitaRepository
import com.pe.terapia.domain.usecase.citas.ObtenerSolicitudesGestionCitaUseCase
import com.pe.terapia.domain.usecase.citas.ResolverSolicitudGestionCitaUseCase
import com.pe.terapia.presentation.admin.solicitudes.SolicitudesGestionCitaViewModel
import com.pe.terapia.domain.repository.RecordatorioCitasScheduler
import com.pe.terapia.domain.usecase.auth.ObtenerUsuarioActualUseCase
import com.pe.terapia.domain.usecase.auth.ObtenerUidSesionActualUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerCitasHoyPsicologoUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerCitasPsicologoResumenUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerDisponibilidadUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerProximaCitaUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerCitasPacienteUseCase
import com.pe.terapia.domain.usecase.citas.SolicitarGestionCitaUseCase
import com.pe.terapia.domain.usecase.citas.CancelarCitaPacienteUseCase
import com.pe.terapia.domain.usecase.citas.TieneSolicitudGestionPendienteUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerSolicitudGestionPendienteUseCase
import com.pe.terapia.domain.usecase.citas.ReservarCitaUseCase
import com.pe.terapia.domain.usecase.citas.MarcarCitaNoAsistioUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerCitasPendientesResultadoUseCase
import com.pe.terapia.domain.usecase.citas.ValidarCitaQrUseCase
import com.pe.terapia.domain.usecase.citas.GuardarProximaCitaEnCacheUseCase
import com.pe.terapia.domain.usecase.citas.ObtenerProximaCitaDeCacheUseCase
import com.pe.terapia.presentation.citas.confirmacion.ConfirmacionCitaViewModel
import com.pe.terapia.presentation.citas.detalle.DetalleCitaViewModel
import com.pe.terapia.presentation.citas.reserva.ReservaCitaViewModel
import com.pe.terapia.presentation.escanner.EscannerViewModel
import com.pe.terapia.presentation.home.paciente.inicio.HomePacienteViewModel
import com.pe.terapia.presentation.home.paciente.miscitas.MisCitasViewModel
import com.pe.terapia.presentation.home.psicologo.inicio.HomePsicologoViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val citaModule = module {
    // 1. Data Sources y Repositories
    single { CitaDataSource(get()) }
    single<CitaRepository> { CitaRepositoryImpl(get()) }
    single { SolicitudesGestionCitaDataSource(get()) }
    single<SolicitudesGestionCitaRepository> { SolicitudesGestionCitaRepositoryImpl(get()) }
    single<RecordatorioCitasScheduler> { WorkManagerRecordatorioCitasScheduler(get()) }

    // 2. Casos de Uso (Use Cases)
    factory { ObtenerDisponibilidadUseCase(get()) }
    factory { ObtenerProximaCitaUseCase(get()) }
    factory { ObtenerCitasPacienteUseCase(get()) }
    factory { SolicitarGestionCitaUseCase(get()) }
    factory { CancelarCitaPacienteUseCase(get()) }
    factory { TieneSolicitudGestionPendienteUseCase(get()) }
    factory { ObtenerSolicitudGestionPendienteUseCase(get()) }
    factory { ObtenerSolicitudesGestionCitaUseCase(get()) }
    factory { ResolverSolicitudGestionCitaUseCase(get()) }
    factory { ReservarCitaUseCase(get()) }
    factory { ObtenerCitasHoyPsicologoUseCase(get()) }
    factory { ObtenerCitasPsicologoResumenUseCase(get()) }
    factory { MarcarCitaNoAsistioUseCase(get()) }
    factory { ObtenerCitasPendientesResultadoUseCase(get()) }
    factory { ValidarCitaQrUseCase(get()) }
    factory { GuardarProximaCitaEnCacheUseCase(get()) }
    factory { ObtenerProximaCitaDeCacheUseCase(get()) }
    // IMPORTANTE: Se eliminó ObtenerUsuarioActualUseCase porque Koin ya lo toma del AuthModule automáticamente.

    // 3. ViewModels simples
    viewModel { ReservaCitaViewModel(get(), get()) }
    viewModel { HomePacienteViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { MisCitasViewModel(get(), get()) }
    viewModel { HomePsicologoViewModel(get(), get(), get(), get(), get()) }
    viewModel { EscannerViewModel(get(), get()) }
    viewModel { SolicitudesGestionCitaViewModel(get(), get(), get()) }

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
            get(),
            get(),
            get(),
            get()
        )
    }

    viewModel { parameters ->
        DetalleCitaViewModel(
            citaId = parameters[0],
            obtenerUsuarioActualUseCase = get(),
            obtenerCitasPacienteUseCase = get(),
            solicitarGestionCitaUseCase = get(),
            obtenerDisponibilidadUseCase = get(),
            tieneSolicitudGestionPendienteUseCase = get(),
            obtenerSolicitudGestionPendienteUseCase = get(),
            cancelarCitaPacienteUseCase = get(),
            recordatorioCitasScheduler = get()
        )
    }
}
