package com.pe.terapia.domain.repository

interface HistoriaClinicaRepository {
    // Verifica si el paciente ya tiene historia, si no, le crea un documento vacío
    suspend fun verificarOCrearHistoria(pacienteId: String): Result<Boolean>
}