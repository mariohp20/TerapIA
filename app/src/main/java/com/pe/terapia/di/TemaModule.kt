package com.pe.terapia.di

import com.pe.terapia.data.repository.PreferenciasTemaRepositoryImpl
import com.pe.terapia.domain.repository.PreferenciasTemaRepository
import com.pe.terapia.presentation.cuenta.TemaViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val temaModule = module {
    single<PreferenciasTemaRepository> { PreferenciasTemaRepositoryImpl(get()) }
    viewModel { TemaViewModel(get()) }
}
