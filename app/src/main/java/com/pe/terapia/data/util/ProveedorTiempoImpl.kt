package com.pe.terapia.data.util

import com.pe.terapia.domain.util.ProveedorTiempo
import java.time.Instant

class ProveedorTiempoImpl : ProveedorTiempo {
    override fun obtenerHoraActual(): Instant {
        // Retorna la hora actual del dispositivo, aunque puede ser modificada por el usuario
        return Instant.now()
    }
}