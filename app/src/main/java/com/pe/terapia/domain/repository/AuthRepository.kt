package com.pe.terapia.domain.repository

import com.pe.terapia.domain.model.Usuario

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Usuario>
    suspend fun registro(email: String, password: String, nombre: String): Result<Usuario>
    suspend fun loginConGoogle(idToken: String): Result<Usuario>
    suspend fun recuperarPassword(email: String): Result<Unit>
    suspend fun obtenerUsuarioActual(): Usuario?
    fun haySesionActiva(): Boolean
    suspend fun cerrarSesion()
}