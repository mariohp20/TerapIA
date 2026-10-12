package com.pe.terapia.domain.repository

import com.pe.terapia.domain.model.Atencion

interface HistoriaClinicaRepository {
    suspend fun registrarAtencion(atencion: Atencion): Result<Unit>
}
