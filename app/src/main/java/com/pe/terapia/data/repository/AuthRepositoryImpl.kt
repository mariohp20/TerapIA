package com.pe.terapia.data.repository

import com.pe.terapia.data.local.UserSessionDataStore
import com.pe.terapia.data.remote.firebase.AuthDataSource
import com.pe.terapia.domain.model.Rol
import com.pe.terapia.domain.model.Usuario
import com.pe.terapia.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val dataSource: AuthDataSource,
    private val userSessionDataStore: UserSessionDataStore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Usuario> = try {
        val uid = dataSource.login(email, password)
        val datos = dataSource.obtenerDatosUsuario(uid)
        val rol = (datos?.get("rol") as? String ?: userSessionDataStore.obtenerRol() ?: Rol.PACIENTE.valor)
        userSessionDataStore.guardarRol(rol)
        Result.success(Usuario(uid, datos?.get("nombre") as? String ?: "", email, rol))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun registro(email: String, password: String, nombre: String): Result<Usuario> = try {
        val uid = dataSource.registro(email, password)
        dataSource.guardarUsuarioEnFirestore(uid, nombre, email, Rol.PACIENTE.valor)
        userSessionDataStore.guardarRol(Rol.PACIENTE.valor)
        Result.success(Usuario(uid, nombre, email, Rol.PACIENTE.valor))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun loginConGoogle(idToken: String): Result<Usuario> = try {
        val uid = dataSource.loginConGoogle(idToken)
        val datos = dataSource.obtenerDatosUsuario(uid)
        val rol = (datos?.get("rol") as? String ?: userSessionDataStore.obtenerRol() ?: Rol.PACIENTE.valor)
        userSessionDataStore.guardarRol(rol)
        Result.success(Usuario(uid, datos?.get("nombre") as? String ?: "", datos?.get("email") as? String ?: "", rol))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun recuperarPassword(email: String): Result<Unit> = try {
        dataSource.recuperarPassword(email)
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun obtenerUsuarioActual(): Usuario? {
        val firebaseUser = dataSource.obtenerUsuarioActual() ?: return null
        val datos = dataSource.obtenerDatosUsuario(firebaseUser.uid)
        val rol = (datos?.get("rol") as? String ?: userSessionDataStore.obtenerRol() ?: Rol.PACIENTE.valor)
        userSessionDataStore.guardarRol(rol)

        return Usuario(
            uid = firebaseUser.uid,
            nombre = (datos?.get("nombre") as? String) ?: (firebaseUser.displayName ?: ""),
            email = (datos?.get("email") as? String) ?: (firebaseUser.email ?: ""),
            rol = rol
        )
    }

    override fun haySesionActiva(): Boolean = dataSource.obtenerUidActual() != null

    override suspend fun cerrarSesion() {
        dataSource.cerrarSesion()
        userSessionDataStore.limpiar()
    }
}