package com.pe.terapia.di

import com.pe.terapia.presentation.home.paciente.perfil.PerfilViewModel
import com.pe.terapia.presentation.home.psicologo.perfil.PerfilPsicologoViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val perfilModule = module {
    viewModel { PerfilViewModel(obtenerUsuarioActualUseCase = get()) }
    viewModel { PerfilPsicologoViewModel(obtenerUsuarioActualUseCase = get()) }
}