package com.pe.terapia.domain.usecase.admin

import com.pe.terapia.domain.repository.AdminRepository

class AsignarHorarioPsicologoUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(
        psicologoId: String,
        sedeId: String,
        diaSemana: Int,
        inicioMinuto: Int,
        finMinuto: Int
    ): Result<Unit> {
        return repository.asignarHorarioPsicologo(psicologoId, sedeId, diaSemana, inicioMinuto, finMinuto)
    }
}