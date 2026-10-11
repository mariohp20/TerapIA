package com.pe.terapia.domain.repository

import com.pe.terapia.domain.model.Usuario
import com.pe.terapia.domain.model.PerfilPacienteBorrador

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Usuario>
    suspend fun registro(
        nombres: String,
        apellidos: String,
        correo: String,
        password: String,
        telefono: String?,
        dni: String
    ): Result<Usuario>
    suspend fun loginConGoogle(idToken: String): Result<Usuario>
    suspend fun completarPerfil(nombres: String, apellidos: String, telefono: String?, dni: String): Result<Usuario>
    suspend fun obtenerBorradorPerfil(): Result<PerfilPacienteBorrador>
    suspend fun recuperarPassword(email: String): Result<Unit>
    suspend fun obtenerUsuarioActual(): Result<Usuario?>
    suspend fun cerrarSesion()
}
