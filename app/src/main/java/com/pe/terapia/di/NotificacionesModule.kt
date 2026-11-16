package com.pe.terapia.di

import com.pe.terapia.domain.usecase.notificaciones.GuardarNotificacionUseCase
import com.pe.terapia.domain.usecase.notificaciones.MarcarNotificacionLeidaUseCase
import com.pe.terapia.domain.usecase.notificaciones.MarcarTodasNotificacionesLeidasUseCase
import com.pe.terapia.domain.usecase.notificaciones.ObservarNotificacionesUseCase
import com.pe.terapia.presentation.notifications.NotificacionesViewModel
import com.pe.terapia.presentation.notifications.RecordatoriosSettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val notificacionesModule = module {
    factory { GuardarNotificacionUseCase(get()) }
    factory { ObservarNotificacionesUseCase(get()) }
    factory { MarcarNotificacionLeidaUseCase(get()) }
    factory { MarcarTodasNotificacionesLeidasUseCase(get()) }

    viewModel { NotificacionesViewModel(get(), get(), get(), get()) }
    viewModel { RecordatoriosSettingsViewModel(get(), get(), get(), get(), get()) }
}
