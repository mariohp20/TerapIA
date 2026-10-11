package com.pe.terapia.domain.repository

interface HistoriaClinicaRepository {
    suspend fun verificarOCrearHistoria(pacienteId: String): Result<Boolean>
}
