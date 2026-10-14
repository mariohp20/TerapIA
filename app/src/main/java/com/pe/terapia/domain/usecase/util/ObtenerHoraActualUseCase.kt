package com.pe.terapia.domain.usecase.util

import com.pe.terapia.domain.util.ProveedorTiempo
import java.time.Instant

class ObtenerHoraActualUseCase (
    private val proveedorTiempo : ProveedorTiempo
){
    operator fun invoke(): Instant{
        return proveedorTiempo.obtenerHoraActual()
    }

}