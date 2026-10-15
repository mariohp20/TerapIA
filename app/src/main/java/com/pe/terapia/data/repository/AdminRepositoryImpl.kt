package com.pe.terapia.data.repository

import com.pe.terapia.data.remote.firebase.AdminDataSource
import com.pe.terapia.domain.repository.AdminRepository
import com.pe.terapia.domain.model.Especialidad
import com.pe.terapia.domain.model.Psicologo
import com.pe.terapia.domain.model.Sede
import com.pe.terapia.domain.model.Usuario

class AdminRepositoryImpl(
    private val adminDataSource: AdminDataSource
) : AdminRepository {

    override suspend fun promoverUsuario(
        usuarioId: String,
        nombres: String,
        apellidos: String,
        descripcionProfesional: String,
        especialidadIds: List<String>,
        sedeIds: List<String>
    ): Result<Unit> {
        return adminDataSource.promoverPacienteAPsicologo(
            usuarioId = usuarioId,
            nombres = nombres,
            apellidos = apellidos,
            descripcionProfesional = descripcionProfesional,
            especialidadIds = especialidadIds,
            sedeIds = sedeIds
        )
    }
    override suspend fun obtenerPacientes(): Result<List<Usuario>> = runCatching {
        adminDataSource.obtenerPacientes()
    }

    override suspend fun obtenerPsicologos(): Result<List<Psicologo>> = runCatching {
        adminDataSource.obtenerPsicologos()
    }

    override suspend fun obtenerSedes(): Result<List<Sede>> = runCatching {
        adminDataSource.obtenerSedesGlobales()
    }

    override suspend fun obtenerEspecialidades(): Result<List<Especialidad>> = runCatching {
        adminDataSource.obtenerEspecialidadesGlobales()
    }
    override suspend fun asignarHorarioPsicologo(
        psicologoId: String,
        sedeId: String,
        diaSemana: Int,
        inicioMinuto: Int,
        finMinuto: Int
    ): Result<Unit> = runCatching {
        adminDataSource.asignarHorarioPsicologo(
            psicologoId,
            sedeId,
            diaSemana,
            inicioMinuto,
            finMinuto
        )
    }
}