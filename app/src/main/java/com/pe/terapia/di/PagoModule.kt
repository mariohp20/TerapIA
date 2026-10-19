package com.pe.terapia.di

import com.pe.terapia.data.remote.firebase.PagoDataSource
import com.pe.terapia.data.repository.PagoRepositoryImpl
import com.pe.terapia.domain.repository.PagoRepository
import com.pe.terapia.domain.usecase.pagos.ConfirmarPagoClinicaUseCase
import com.pe.terapia.domain.usecase.pagos.ObtenerComprobantePorCitaUseCase
import com.pe.terapia.domain.usecase.pagos.ObtenerHistorialPagosUseCase
import com.pe.terapia.domain.usecase.pagos.ObtenerPagosPendientesPsicologoUseCase
import com.pe.terapia.domain.usecase.pagos.PagarOnlineUseCase
import com.pe.terapia.presentation.pagos.GestionPagosPsicologoViewModel
import com.pe.terapia.presentation.pagos.HistorialPagosViewModel
import com.pe.terapia.presentation.pagos.PasarelaPagoViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val pagoModule = module {
    single { PagoDataSource(get()) }
    single<PagoRepository> { PagoRepositoryImpl(get()) }

    factory { ObtenerHistorialPagosUseCase(get()) }
    factory { ObtenerPagosPendientesPsicologoUseCase(get()) }
    factory { ConfirmarPagoClinicaUseCase(get()) }
    factory { PagarOnlineUseCase(get()) }
    factory { ObtenerComprobantePorCitaUseCase(get()) }

    viewModel { HistorialPagosViewModel(get(), get(), get(), get(), get()) }
    viewModel { (pagoId: String) -> PasarelaPagoViewModel(pagoId, get()) }
    viewModel { GestionPagosPsicologoViewModel(get(), get(), get()) }
}
