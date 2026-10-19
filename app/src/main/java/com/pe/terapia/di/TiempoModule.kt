package com.pe.terapia.di

import com.pe.terapia.data.util.ProveedorTiempoImpl
import com.pe.terapia.domain.usecase.util.ObtenerHoraActualUseCase
import com.pe.terapia.domain.util.ProveedorTiempo
import org.koin.dsl.module

val tiempoModule = module {
    single<ProveedorTiempo> { ProveedorTiempoImpl() }

    factory { ObtenerHoraActualUseCase(get()) }
}