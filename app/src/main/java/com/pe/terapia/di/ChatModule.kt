package com.pe.terapia.di

import com.pe.terapia.data.remote.firebase.GeminiDataSource
import com.pe.terapia.data.repository.ChatRepositoryImpl
import com.pe.terapia.domain.repository.ChatRepository
import com.pe.terapia.domain.usecase.chat.EnviarMensajeChatUseCase
import com.pe.terapia.presentation.home.paciente.chat.TerapiaChatViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val chatModule = module {
    single { GeminiDataSource() }
    single<ChatRepository> { ChatRepositoryImpl(get()) }
    factory { EnviarMensajeChatUseCase(get()) }
    viewModel { TerapiaChatViewModel(get()) }
}
