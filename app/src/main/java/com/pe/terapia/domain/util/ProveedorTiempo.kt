package com.pe.terapia.domain.util

import java.time.Instant

interface ProveedorTiempo {
    fun obtenerHoraActual(): Instant
}