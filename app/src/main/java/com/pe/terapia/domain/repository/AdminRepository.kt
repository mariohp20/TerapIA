package com.pe.terapia.domain.repository

import com.pe.terapia.domain.model.Especialidad
import com.pe.terapia.domain.model.Psicologo
import com.pe.terapia.domain.model.Sede
import com.pe.terapia.domain.model.Usuario

interface AdminRepository {
    suspend fun promoverUsuario(
        usuarioId: String,
        nombres: String,
        apellidos: String,
        descripcionProfesional: String,
        especialidadIds: List<String>,
        sedeIds: List<String>
    ): Result<Unit>
    suspend fun obtenerPacientes(): Result<List<Usuario>>
    suspend fun obtenerPsicologos(): Result<List<Psicologo>>
    suspend fun obtenerSedes(): Result<List<Sede>>
    suspend fun obtenerEspecialidades(): Result<List<Especialidad>>
    suspend fun asignarHorarioPsicologo(
        psicologoId: String,
        sedeId: String,
        diaSemana: Int,
        inicioMinuto: Int,
        finMinuto: Int
    ): Result<Unit>
}