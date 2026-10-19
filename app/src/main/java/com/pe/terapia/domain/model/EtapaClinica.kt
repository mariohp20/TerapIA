package com.pe.terapia.domain.model

import java.time.Instant

data class EtapaClinica(
    val id: String,
    val procesoTerapeuticoId: String,
    val tipoEtapaId: String,
    val estado: EstadoEtapa,
    val fechaInicio: Instant? = null,
    val fechaFin: Instant? = null,
    val objetivosEspecificos: String? = null,
    val observaciones: String? = null
)