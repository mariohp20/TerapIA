package com.pe.terapia.domain.model

import java.time.Instant

data class AtencionResumenDashboard(
    val citaId: String,
    val fechaAtencion: Instant
)
