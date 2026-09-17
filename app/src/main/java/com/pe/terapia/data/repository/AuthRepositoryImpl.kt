package com.pe.terapia.data.repository

import com.pe.terapia.data.remote.firebase.AuthDataSource
import com.pe.terapia.domain.model.Rol
import com.pe.terapia.domain.model.Usuario
import com.pe.terapia.domain.repository.AuthRepository

class AuthRepositoryImpl(private val dataSource: AuthDataSource) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Usuario> = try {
        val uid = dataSource.login(email, password)
        val datos = dataSource.obtenerDatosUsuario(uid)
        Result.success(Usuario(uid, datos?.get("nombre") as? String ?: "", email, datos?.get("rol") as? String ?: ""))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun registro(email: String, password: String, nombre: String): Result<Usuario> = try {
        val uid = dataSource.registro(email, password)
        dataSource.guardarUsuarioEnFirestore(uid, nombre, email, Rol.PACIENTE.valor)
        Result.success(Usuario(uid, nombre, email, Rol.PACIENTE.valor))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun loginConGoogle(idToken: String): Result<Usuario> = try {
        val uid = dataSource.loginConGoogle(idToken)
        val datos = dataSource.obtenerDatosUsuario(uid)
        Result.success(Usuario(uid, datos?.get("nombre") as? String ?: "", datos?.get("email") as? String ?: "", datos?.get("rol") as? String ?: ""))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun recuperarPassword(email: String): Result<Unit> = try {
        dataSource.recuperarPassword(email)
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override fun obtenerUsuarioActual(): Usuario? = null
    override fun haySesionActiva(): Boolean = dataSource.obtenerUidActual() != null
    override fun cerrarSesion() = dataSource.cerrarSesion()
}