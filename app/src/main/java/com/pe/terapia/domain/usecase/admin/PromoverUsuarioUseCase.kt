package com.pe.terapia.domain.usecase.admin

import com.pe.terapia.domain.repository.AdminRepository

class PromoverUsuarioUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(
        usuarioId: String,
        nombres: String,
        apellidos: String,
        descripcionProfesional: String,
        especialidadIds: List<String>,
        sedeIds: List<String>
    ): Result<Unit> {

        // Validaciones
        if (usuarioId.isBlank() || nombres.isBlank() || apellidos.isBlank()) {
            return Result.failure(IllegalArgumentException("Los datos básicos del usuario son obligatorios."))
        }
        if (descripcionProfesional.isBlank()) {
            return Result.failure(IllegalArgumentException("Debe ingresar una descripción profesional para el psicólogo."))
        }
        if (especialidadIds.isEmpty()) {
            return Result.failure(IllegalArgumentException("Debe seleccionar al menos una especialidad."))
        }
        if (sedeIds.isEmpty()) {
            return Result.failure(IllegalArgumentException("Debe seleccionar al menos una sede de atención."))
        }

        // Ejecucion si todo OK
        return repository.promoverUsuario(
            usuarioId = usuarioId.trim(),
            nombres = nombres.trim(),
            apellidos = apellidos.trim(),
            descripcionProfesional = descripcionProfesional.trim(),
            especialidadIds = especialidadIds,
            sedeIds = sedeIds
        )
    }
}